package org.netra.aws_project.util.aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AWSDestroyVPC {

//    private static final String REGION = "eu-north-1";

    private final AmazonEC2 ec2;

    public AWSDestroyVPC() {
        this.ec2 = getEc2Client();
    }

    public AmazonEC2 getEc2Client(){
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException("Les identifiants IAM ne sont pas localisés dans votre Home Directory.", e);
        }
        return AmazonEC2ClientBuilder.standard()
            .withCredentials(credentialsProvider)
            .withRegion(Regions.EU_NORTH_1)
            .build();
    }

    public void deleteInstance(String instanceId) {
        TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest().withInstanceIds(instanceId);
        ec2.terminateInstances(terminateInstancesRequest);
        System.out.println("Instance supprimée avec l'ID : " + instanceId);
    }

    public void deleteSubnet(String subnetId) {
        DeleteSubnetRequest deleteSubnetRequest = new DeleteSubnetRequest().withSubnetId(subnetId);
        ec2.deleteSubnet(deleteSubnetRequest);
        System.out.println("Sous-réseau supprimé avec l'ID : " + subnetId);
    }

    public void deleteInternetGateway(String igwId, String vpcId) {
        DetachInternetGatewayRequest detachInternetGatewayRequest = new DetachInternetGatewayRequest()
            .withInternetGatewayId(igwId)
            .withVpcId(vpcId);
        ec2.detachInternetGateway(detachInternetGatewayRequest);
        DeleteInternetGatewayRequest deleteInternetGatewayRequest = new DeleteInternetGatewayRequest().withInternetGatewayId(igwId);
        ec2.deleteInternetGateway(deleteInternetGatewayRequest);
        System.out.println("Passerelle Internet supprimée avec l'ID : " + igwId);
    }

    public void deleteRouteTable(String routeTableId) {
        DeleteRouteTableRequest deleteRouteTableRequest = new DeleteRouteTableRequest().withRouteTableId(routeTableId);
        ec2.deleteRouteTable(deleteRouteTableRequest);
        System.out.println("Table de routage supprimée avec l'ID : " + routeTableId);
    }

    public void deleteVPC(String vpcId) {
        DeleteVpcRequest deleteVpcRequest = new DeleteVpcRequest().withVpcId(vpcId);
        ec2.deleteVpc(deleteVpcRequest);
        System.out.println("VPC supprimé avec l'ID : " + vpcId);
    }

    public void deleteAllResources(String instanceId, String routeTableId, List<String> subnetIds, String igwId, String vpcId) {
        // Supprimer l'instance
        if (instanceId != null && !instanceId.isEmpty()){
            deleteInstance(instanceId);
            try { Thread.sleep(60000); } catch (InterruptedException e) { e.printStackTrace(); }
        }

        // Détacher et supprimer la passerelle Internet
        if(igwId != null && !subnetIds.isEmpty() && internetGatewayExist(igwId)){
            deleteInternetGateway(igwId, vpcId);
            try { Thread.sleep(15000); } catch (InterruptedException e) { e.printStackTrace(); }
        }

        // Supprimer les sous-réseau
        for (String subnetId : subnetIds) {
            if (subnetExist(subnetId)){
                deleteSubnet(subnetId);
                try {
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        // Supprimer la table de routage
        if (routeTableId != null && !routeTableId.isEmpty() && routeTableExist(routeTableId)){
            deleteRouteTable(routeTableId);
            try { Thread.sleep(5000); } catch (InterruptedException e) { e.printStackTrace(); }
        }

        // Supprimer le VPC
        deleteVPC(vpcId);
    }

    public boolean subnetExist(String subnetId){

        try {
            // Vérifier l'existence du subnet
            DescribeSubnetsRequest subnetRequest = new DescribeSubnetsRequest().withSubnetIds(subnetId);
            DescribeSubnetsResult subnetResult = ec2.describeSubnets(subnetRequest);
            if (!subnetResult.getSubnets().isEmpty()) {
                System.out.println("Le subnet existe.");
                return true;
            } else {
                System.out.println("Le subnet n'existe pas.");
                return false;
            }
        }catch (AmazonEC2Exception e){
            e.printStackTrace();
        }

        return false;
    }

    public boolean internetGatewayExist(String gatewayId){
        // Vérifier l'existence de la gateway
        try {
            DescribeInternetGatewaysRequest gatewayRequest = new DescribeInternetGatewaysRequest().withInternetGatewayIds(gatewayId);
            DescribeInternetGatewaysResult gatewayResult = ec2.describeInternetGateways(gatewayRequest);
            if (!gatewayResult.getInternetGateways().isEmpty()) {
                System.out.println("La gateway existe.");
                return true;
            } else {
                System.out.println("La gateway n'existe pas.");
                return false;
            }
        }catch (AmazonEC2Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean routeTableExist(String routeTableId){

        try {
            // Vérifier l'existence de la table de routage
            DescribeRouteTablesRequest routeTableRequest = new DescribeRouteTablesRequest().withRouteTableIds(routeTableId);
            DescribeRouteTablesResult routeTableResult = ec2.describeRouteTables(routeTableRequest);

            if (!routeTableResult.getRouteTables().isEmpty()) {
                System.out.println("La table de routage existe.");
                return true;
            } else {
                System.out.println("La table de routage n'existe pas.");
                return false;
            }
        }catch (AmazonEC2Exception e){
            e.printStackTrace();
        }

        return false;
    }

}
