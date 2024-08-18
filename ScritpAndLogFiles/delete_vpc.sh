#!/bin/bash

clear

# Afficher l'ASCII art
cat << "EOF"

__ __| |   |_ _|  _ \   _ \             \\ \        / ___|  
   |   |   |  |  |   | |   |           _ \\ \  \   /\___ \  
   |   ___ |  |  |   | __ <  _____|   ___ \\ \  \ /       | 
  _|  _|  _|___|\___/ _| \_\        _/    _\\_/\_/  _____/  
                                                            
      ___|  _ \  ____|    \ __ __|_ _|  _ \   \  | 
     |     |   | __|     _ \   |    |  |   |   \ | 
     |     __ <  |      ___ \  |    |  |   | |\  | 
    \____|_| \_\_____|_/    _\_|  ___|\___/ _| \_| 
  
EOF

# Fonction pour afficher le titre en ASCII art
function print_ascii_art {
    clear
    echo "**********************************************************************************"
    figlet "      THIOR AWS ©️ "
    figlet "Interface de"
    figlet "suppression de VPCs"
    echo "**********************************************************************************"
}

print_ascii_art

# Fonction pour supprimer une instance EC2
delete_instance() {
    local instance_id=$1
    echo "Suppression de l'instance EC2 avec l'ID : $instance_id"
    aws ec2 terminate-instances --instance-ids $instance_id
    aws ec2 wait instance-terminated --instance-ids $instance_id
    echo "Instance EC2 supprimée : $instance_id"
}

# Fonction pour supprimer un groupe de sécurité
delete_security_group() {
    local sg_id=$1
    echo "Suppression du groupe de sécurité avec l'ID : $sg_id"
    aws ec2 delete-security-group --group-id $sg_id
    echo "Groupe de sécurité supprimé : $sg_id"
}

# Fonction pour détacher et supprimer une passerelle Internet
delete_internet_gateway() {
    local igw_id=$1
    local vpc_id=$2
    echo "Détachement et suppression de la passerelle Internet avec l'ID : $igw_id"
    aws ec2 detach-internet-gateway --internet-gateway-id $igw_id --vpc-id $vpc_id
    aws ec2 delete-internet-gateway --internet-gateway-id $igw_id
    echo "Passerelle Internet supprimée : $igw_id"
}

# Fonction pour supprimer un sous-réseau
delete_subnet() {
    local subnet_id=$1
    echo "Suppression du sous-réseau avec l'ID : $subnet_id"
    aws ec2 delete-subnet --subnet-id $subnet_id
    echo "Sous-réseau supprimé : $subnet_id"
}

# Fonction pour supprimer une table de routage
delete_route_table() {
    local rt_id=$1
    echo "Suppression de la table de routage avec l'ID : $rt_id"
    aws ec2 delete-route-table --route-table-id $rt_id
    echo "Table de routage supprimée : $rt_id"
}

# Fonction pour supprimer un VPC
delete_vpc() {
    local vpc_id=$1
    echo "Suppression du VPC avec l'ID : $vpc_id"
    aws ec2 delete-vpc --vpc-id $vpc_id
    echo "VPC supprimé : $vpc_id"
}

# Demander les informations à l'utilisateur
read -p "Veuillez entrer l'ID du VPC à supprimer : " VPC_ID
read -p "Veuillez entrer l'ID de la passerelle Internet à supprimer : " IGW_ID

# Demander les IDs des instances à supprimer
echo "Veuillez entrer les IDs des instances à supprimer (séparés par des espaces) : "
read -a INSTANCE_IDS

# Supprimer les instances
for instance_id in "${INSTANCE_IDS[@]}"; do
    delete_instance $instance_id
done

# Liste des groupes de sécurité associés au VPC
security_groups=$(aws ec2 describe-security-groups --filters "Name=vpc-id,Values=$VPC_ID" --query 'SecurityGroups[*].GroupId' --output text)
echo "Groupes de sécurité trouvés dans le VPC : $security_groups"

# Supprimer les groupes de sécurité
for sg_id in $security_groups; do
    delete_security_group $sg_id
done

# Liste des sous-réseaux associés au VPC
subnets=$(aws ec2 describe-subnets --filters "Name=vpc-id,Values=$VPC_ID" --query 'Subnets[*].SubnetId' --output text)
echo "Sous-réseaux trouvés dans le VPC : $subnets"

# Supprimer les sous-réseaux
for subnet_id in $subnets; do
    delete_subnet $subnet_id
done

# Liste des tables de routage associées au VPC
route_tables=$(aws ec2 describe-route-tables --filters "Name=vpc-id,Values=$VPC_ID" --query 'RouteTables[*].RouteTableId' --output text)
echo "Tables de routage trouvées dans le VPC : $route_tables"

# Supprimer les tables de routage
for rt_id in $route_tables; do
    delete_route_table $rt_id
done

# Supprimer la passerelle Internet
delete_internet_gateway $IGW_ID $VPC_ID

# Supprimer le VPC
delete_vpc $VPC_ID

echo "Toutes les ressources associées au VPC $VPC_ID ont été supprimées."
