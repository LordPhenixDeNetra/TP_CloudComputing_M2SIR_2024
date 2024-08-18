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
    # figlet -f slant "Interface d'ajout d'un Utilisateur"
    echo "**********************************************************************************"
    figlet "      THIOR AWS ©️ "
    figlet "Interface de"
    figlet "creation de VPCs"
    echo "**********************************************************************************"
}

print_ascii_art

# Fonction pour vérifier le format CIDR
validate_cidr() {
    local cidr=$1
    if [[ ! $cidr =~ ^([0-9]{1,3}\.){3}[0-9]{1,3}/[0-9]{1,2}$ ]]; then
        echo "CIDR invalide : $cidr"
        return 1
    fi

    local ip=${cidr%/*}
    local prefix=${cidr#*/}
    IFS='.' read -r -a octets <<< "$ip"

    for octet in "${octets[@]}"; do
        if ((octet < 0 || octet > 255)); then
            echo "CIDR invalide : $cidr"
            return 1
        fi
    done

    if ((prefix < 0 || prefix > 32)); then
        echo "CIDR invalide : $cidr"
        return 1
    fi

    return 0
}

# Fonction pour vérifier si la région est valide
validate_region() {
    local region=$1
    local valid_regions=("us-east-1" "us-west-1" "us-west-2" "eu-west-1" "eu-central-1" "eu-north-1")
    for valid_region in "${valid_regions[@]}"; do
        if [[ $region == $valid_region ]]; then
            return 0
        fi
    done
    echo "Région invalide : $region"
    return 1
}

# Fonction pour obtenir l'AMI la plus récente
get_latest_ami() {
    local os=$1
    case $os in
        "Ubuntu")
            aws ec2 describe-images \
                --owners 099720109477 \
                --filters "Name=name,Values=ubuntu/images/hvm-ssd/ubuntu-focal-20.04-amd64-server-*" \
                --query 'sort_by(Images, &CreationDate)[-1].ImageId' \
                --output text
            ;;
        "Amazon Linux")
            aws ec2 describe-images \
                --owners amazon \
                --filters "Name=name,Values=amzn2-ami-hvm-2.0.*-x86_64-gp2" \
                --query 'sort_by(Images, &CreationDate)[-1].ImageId' \
                --output text
            ;;
        "Windows")
            aws ec2.describe-images \
                --owners amazon \
                --filters "Name=name,Values=Windows_Server-2019-English-Full-Base-*" \
                --query 'sort_by(Images, &CreationDate)[-1].ImageId' \
                --output text
            ;;
    esac
}

# Fonction pour créer une instance
create_instance() {
    local log_file=$1
    local region=$2
    local instance_name=$3
    local instance_type=$4
    local os_type=$5
    local storage_size=$6
    local subnet_id=$7
    local security_group=$8
    local public_ip_option=$9

    # Obtenir l'AMI la plus récente
    local ami_id=$(get_latest_ami "$os_type")
    echo "AMI sélectionnée : $ami_id"

    # Préparer l'option pour l'adresse IP publique
    local public_ip_config
    if [ "$public_ip_option" = "Oui" ]; then
        public_ip_config="--associate-public-ip-address"
    else
        public_ip_config="--no-associate-public-ip-address"
    fi

    # Créer l'instance
    echo "Création de l'instance EC2..."
    local instance_id=$(aws ec2 run-instances \
        --image-id $ami_id \
        --instance-type $instance_type \
        --tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=$instance_name}]" \
        --security-group-ids $security_group \
        --subnet-id $subnet_id \
        $public_ip_config \
        --block-device-mappings "[{\"DeviceName\":\"/dev/sda1\",\"Ebs\":{\"VolumeSize\":$storage_size}}]" \
        --output text \
        --query 'Instances[0].InstanceId')

    if [ $? -eq 0 ]; then
        echo "Instance EC2 créée avec succès. ID de l'instance : $instance_id"

        # Attendre que l'instance soit en cours d'exécution
        echo "Attente du démarrage de l'instance..."
        aws ec2 wait instance-running --instance-ids $instance_id

        # Obtenir l'adresse IP publique de l'instance (si demandée)
        if [ "$public_ip_option" = "Oui" ]; then
            local public_ip=$(aws ec2.describe-instances \
                --instance-ids $instance_id \
                --output text \
                --query 'Reservations[0].Instances[0].PublicIpAddress')
            echo "Adresse IP publique: $public_ip"
        fi

        echo "L'instance est en cours d'exécution avec l' ID: $instance_id" | tee -a $log_file
        echo "=================================="
        echo "Nom: $instance_name"
        echo "ID: $instance_id"
        echo "Type: $instance_type"
        echo "OS: $os_type"
        echo "Stockage: $storage_size Go"
    else
        echo "Échec de la création de l'instance EC2."
    fi
}

# Demander les informations à l'utilisateur et valider
while true; do
    read -p "Veuillez entrer le nom du VPC : " VPC_NAME
    read -p "Veuillez entrer le CIDR du VPC (ex: 10.0.0.0/16) : " VPC_CIDR
    validate_cidr $VPC_CIDR && break
done

while true; do
    read -p "Veuillez entrer le CIDR du sous-réseau public (ex: 10.0.1.0/24) : " SUBNET_PUBLIC_CIDR
    validate_cidr $SUBNET_PUBLIC_CIDR && break
done

while true; do
    read -p "Veuillez entrer le CIDR du sous-réseau privé (ex: 10.0.2.0/24) : " SUBNET_PRIVATE_CIDR
    validate_cidr $SUBNET_PRIVATE_CIDR && break
done

while true; do
    read -p "Veuillez entrer le nom du sous-réseau public : " SUBNET_PUBLIC_NAME
    [[ ! -z $SUBNET_PUBLIC_NAME ]] && break
done

while true; do
    read -p "Veuillez entrer le nom du sous-réseau privé : " SUBNET_PRIVATE_NAME
    [[ ! -z $SUBNET_PRIVATE_NAME ]] && break
done

while true; do
    read -p "Veuillez entrer la région AWS (ex: eu-north-1) : " REGION
    validate_region $REGION && break
done

# Chemin du fichier de log
LOG_FILE="vpc_creation.log"

# Créer le VPC
VPC_ID=$(aws ec2 create-vpc --cidr-block $VPC_CIDR --query 'Vpc.VpcId' --output text --region $REGION --tag-specifications "ResourceType=vpc,Tags=[{Key=Name,Value=$VPC_NAME}]")
echo "VPC créé avec l'ID : $VPC_ID" | tee -a $LOG_FILE

# Création des groupes de sécurité pour le réseau public et privé
echo "Entrez le nom du groupe de sécurité pour le réseau public : "
read public_sg_name
echo "Entrez le nom du groupe de sécurité pour le réseau privé : "
read private_sg_name

# Création du groupe de sécurité pour le réseau public
echo "Création du groupe de sécurité $public_sg_name pour le réseau public ..."
public_sg_id=$(aws ec2 create-security-group --group-name $public_sg_name --description "Security group for public subnet" --vpc-id $VPC_ID --output text)
echo "Groupe de sécurité public créé avec l'ID : $public_sg_id"

# Création du groupe de sécurité pour le réseau privé
echo "Création du groupe de sécurité $private_sg_name pour le réseau privé ..."
private_sg_id=$(aws ec2 create-security-group --group-name $private_sg_name --description "Security group for private subnet" --vpc-id $VPC_ID --output text)
echo "Groupe de sécurité privé créé avec l'ID : $private_sg_id"

# Créer une passerelle Internet et attacher au VPC
IGW_ID=$(aws ec2 create-internet-gateway --query 'InternetGateway.InternetGatewayId' --output text --region $REGION)
aws ec2 attach-internet-gateway --internet-gateway-id $IGW_ID --vpc-id $VPC_ID --region $REGION
echo "Passerelle Internet créée et attachée avec l'ID : $IGW_ID" | tee -a $LOG_FILE

# Créer un sous-réseau public et un sous-réseau privé
SUBNET_PUBLIC_ID=$(aws ec2 create-subnet --vpc-id $VPC_ID --cidr-block $SUBNET_PUBLIC_CIDR --query 'Subnet.SubnetId' --output text --region $REGION --availability-zone ${REGION}a --tag-specifications "ResourceType=subnet,Tags=[{Key=Name,Value=$SUBNET_PUBLIC_NAME}]")
echo "Sous-réseau public créé avec l'ID : $SUBNET_PUBLIC_ID" | tee -a $LOG_FILE

SUBNET_PRIVATE_ID=$(aws ec2 create-subnet --vpc-id $VPC_ID --cidr-block $SUBNET_PRIVATE_CIDR --query 'Subnet.SubnetId' --output text --region $REGION --availability-zone ${REGION}a --tag-specifications "ResourceType=subnet,Tags=[{Key=Name,Value=$SUBNET_PRIVATE_NAME}]")
echo "Sous-réseau privé créé avec l'ID : $SUBNET_PRIVATE_ID" | tee -a $LOG_FILE

# Créer une table de routage pour le sous-réseau public
ROUTE_TABLE_PUBLIC_ID=$(aws ec2 create-route-table --vpc-id $VPC_ID --query 'RouteTable.RouteTableId' --output text --region $REGION)
aws ec2 associate-route-table --route-table-id $ROUTE_TABLE_PUBLIC_ID --subnet-id $SUBNET_PUBLIC_ID --region $REGION
aws ec2 create-route --route-table-id $ROUTE_TABLE_PUBLIC_ID --destination-cidr-block 0.0.0.0/0 --gateway-id $IGW_ID --region $REGION
echo "Table de routage pour le sous-réseau public créée avec l'ID : $ROUTE_TABLE_PUBLIC_ID" | tee -a $LOG_FILE

# Demander le nom de l'instance publique à l'utilisateur
echo "============================================================================================================="
read -p "Entrez le nom de l'instance publique : " INSTANCE_PUBLIC_NAME
read -p "Entrez le type de l'instance publique (ex: t2.micro) : " INSTANCE_PUBLIC_TYPE
read -p "Entrez le type d'OS pour l'instance publique (Ubuntu, Amazon Linux, Windows) : " INSTANCE_PUBLIC_OS
read -p "Entrez la taille de stockage (en Go) pour l'instance publique : " INSTANCE_PUBLIC_STORAGE
read -p "L'instance publique doit-elle avoir une IP publique ? (Oui/Non) : " INSTANCE_PUBLIC_IP_OPTION
echo "============================================================================================================="

# Demander le nom de l'instance privée à l'utilisateur
echo "============================================================================================================="
read -p "Entrez le nom de l'instance privée : " INSTANCE_PRIVATE_NAME
read -p "Entrez le type de l'instance privée (ex: t2.micro) : " INSTANCE_PRIVATE_TYPE
read -p "Entrez le type d'OS pour l'instance privée (Ubuntu, Amazon Linux, Windows) : " INSTANCE_PRIVATE_OS
read -p "Entrez la taille de stockage (en Go) pour l'instance privée : " INSTANCE_PRIVATE_STORAGE
read -p "L'instance privée doit-elle avoir une IP publique ? (Oui/Non) : " INSTANCE_PRIVATE_IP_OPTION
echo "============================================================================================================="

# Liste des groupes de sécurité disponibles
security_groups=$(aws ec2 describe-security-groups --query 'SecurityGroups[*].[GroupName,GroupId]' --output text --region $REGION)

# Afficher les groupes de sécurité disponibles
echo "Groupes de sécurité disponibles :"
echo "$security_groups"



# Demander à l'utilisateur de choisir un groupe de sécurité pour l'instance publique
while true; do
    read -p "Veuillez entrer l'ID du groupe de sécurité pour l'instance publique : " PUBLIC_SECURITY_GROUP_ID
    if echo "$security_groups" | grep -q "$PUBLIC_SECURITY_GROUP_ID"; then
        break
    else
        echo "ID du groupe de sécurité invalide. Veuillez réessayer."
    fi
done

# Demander à l'utilisateur de choisir un groupe de sécurité pour l'instance privée
while true; do
    read -p "Veuillez entrer l'ID du groupe de sécurité pour l'instance privée : " PRIVATE_SECURITY_GROUP_ID
    if echo "$security_groups" | grep -q "$PRIVATE_SECURITY_GROUP_ID"; then
        break
    else
        echo "ID du groupe de sécurité invalide. Veuillez réessayer."
    fi
done



# Créer les instances EC2
create_instance $LOG_FILE $REGION "$INSTANCE_PUBLIC_NAME" $INSTANCE_PUBLIC_TYPE $INSTANCE_PUBLIC_OS $INSTANCE_PUBLIC_STORAGE $SUBNET_PUBLIC_ID $PUBLIC_SECURITY_GROUP_ID $INSTANCE_PUBLIC_IP_OPTION
create_instance $LOG_FILE $REGION "$INSTANCE_PRIVATE_NAME" $INSTANCE_PRIVATE_TYPE $INSTANCE_PRIVATE_OS $INSTANCE_PRIVATE_STORAGE $SUBNET_PRIVATE_ID $PRIVATE_SECURITY_GROUP_ID $INSTANCE_PRIVATE_IP_OPTION

echo "=========================================================================" | tee -a $LOG_FILE
echo "Script terminé."


