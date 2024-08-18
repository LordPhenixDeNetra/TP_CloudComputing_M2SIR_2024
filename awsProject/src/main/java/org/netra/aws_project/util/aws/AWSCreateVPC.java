package org.netra.aws_project.util.aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import org.springframework.stereotype.Service;


@Service
public class AWSCreateVPC {

    private static final String REGION = "eu-north-1";
    private static final String UBUNTU_AMI_ID = "ami-07a0715df72e58928";
    private static final String INSTANCE_TYPE = "t3.micro";
    private static final String KEY_PAIR_NAME = "MyKeyPair";

    private final AmazonEC2 ec2;

    public AWSCreateVPC() {
        this.ec2 = getEc2Client();
    }

    public AmazonEC2 getEc2Client(){
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException("Les identifiants IAM ne sont pas localisés dans votre Home Directory. ",e);
        }
        return AmazonEC2ClientBuilder.standard()
            .withCredentials(credentialsProvider)
            .withRegion(Regions.EU_NORTH_1)
            .build();
    }

    public String createVPC(String cidrBlock, String name) {
        CreateVpcRequest createVpcRequest = new CreateVpcRequest().withCidrBlock(cidrBlock);
        Vpc vpc = ec2.createVpc(createVpcRequest).getVpc();

        ec2.createTags(new CreateTagsRequest()
            .withResources(vpc.getVpcId())
            .withTags(new Tag("Name", name)));

        System.out.println("VPC créé avec l'ID : " + vpc.getVpcId());
        return vpc.getVpcId();
    }

    public String createSubnet(String vpcId, String cidrBlock, String subnetName) {

        CreateSubnetRequest createSubnetRequest = new CreateSubnetRequest()
            .withVpcId(vpcId)
            .withCidrBlock(cidrBlock);

        Subnet subnet = ec2.createSubnet(createSubnetRequest).getSubnet();

        ec2.createTags(new CreateTagsRequest()
            .withResources(subnet.getSubnetId())
            .withTags(new Tag("Name", subnetName)));

        System.out.println("Sous-réseau créé avec l'ID : " + subnet.getSubnetId());
        return subnet.getSubnetId();
    }

    public String createInternetGateway(String vpcId) {
        CreateInternetGatewayRequest createInternetGatewayRequest = new CreateInternetGatewayRequest();
        InternetGateway internetGateway = ec2.createInternetGateway(createInternetGatewayRequest).getInternetGateway();
        System.out.println("Passerelle Internet créée avec l'ID : " + internetGateway.getInternetGatewayId());

        AttachInternetGatewayRequest attachInternetGatewayRequest = new AttachInternetGatewayRequest()
            .withVpcId(vpcId)
            .withInternetGatewayId(internetGateway.getInternetGatewayId());
        ec2.attachInternetGateway(attachInternetGatewayRequest);
        System.out.println("Passerelle Internet attachée au VPC");
        return internetGateway.getInternetGatewayId();
    }

    public String createRouteTable(String subnetId, String igwId, String vpcId, boolean isPublic) {
        CreateRouteTableRequest createRouteTableRequest = new CreateRouteTableRequest().withVpcId(vpcId);
        RouteTable routeTable = ec2.createRouteTable(createRouteTableRequest).getRouteTable();

        if (isPublic) {
            CreateRouteRequest createRouteRequest = new CreateRouteRequest()
                .withRouteTableId(routeTable.getRouteTableId())
                .withDestinationCidrBlock("0.0.0.0/0")
                .withGatewayId(igwId);
            ec2.createRoute(createRouteRequest);
        }

        AssociateRouteTableRequest associateRouteTableRequest = new AssociateRouteTableRequest()
            .withSubnetId(subnetId)
            .withRouteTableId(routeTable.getRouteTableId());
        ec2.associateRouteTable(associateRouteTableRequest);
        System.out.println("Table de routage pour le sous-réseau " + (isPublic ? "public" : "privé") + " créée avec l'ID : " + routeTable.getRouteTableId());

        if (isPublic) {
            ModifySubnetAttributeRequest modifySubnetAttributeRequest = new ModifySubnetAttributeRequest()
                .withSubnetId(subnetId)
                .withMapPublicIpOnLaunch(true);
            ec2.modifySubnetAttribute(modifySubnetAttributeRequest);
            System.out.println("Mappage d'IP public activé pour le sous-réseau public");
        }

        return  routeTable.getRouteTableId();
    }

    public String createInstance(String subnetId, String amiId, boolean isPublic) {
        RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
            .withImageId(amiId)
            .withInstanceType(INSTANCE_TYPE)
            .withMinCount(1)
            .withMaxCount(1)
//            .withKeyName(KEY_PAIR_NAME)
            .withSubnetId(subnetId)
            ;
//                .withAssociatePublicIpAddress(isPublic);
        Instance instance = ec2.runInstances(runInstancesRequest).getReservation().getInstances().get(0);
        System.out.println("Instance " + (isPublic ? "publique" : "privée") + " créée avec l'ID : " + instance.getInstanceId());

        return instance.getInstanceId();
    }

    public String createInstanceWithoutId(String subnetId, String instanceName,boolean isPublic) {

        RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
            .withImageId(UBUNTU_AMI_ID)
            .withInstanceType(InstanceType.T3Micro)
            .withMinCount(1)
            .withMaxCount(1)
            .withSubnetId(subnetId);
        Instance instance = ec2.runInstances(runInstancesRequest).getReservation().getInstances().get(0);

        ec2.createTags(new CreateTagsRequest()
            .withResources(instance.getInstanceId())
            .withTags(new Tag("Name", instanceName)));

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (isPublic){
            AllocateAddressResult allocateAddressResult = ec2.allocateAddress(new AllocateAddressRequest());
            String allocationId = allocateAddressResult.getAllocationId();

            System.out.println("=======================================");
            System.out.println("IP Adresse : " + allocationId);
            System.out.println("======================================");

            AssociateAddressRequest associateAddressRequest = new AssociateAddressRequest()
                .withInstanceId(instance.getInstanceId())
                .withAllocationId(allocationId);

            ec2.associateAddress(associateAddressRequest);
        }

        System.out.println("Instance " + (isPublic ? "publique" : "privée") + " créée avec l'ID : " + instance.getInstanceId());

        return instance.getInstanceId();
    }

}
