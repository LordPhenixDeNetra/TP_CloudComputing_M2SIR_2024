package org.netra.aws_project.util.aws.config;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AWSConf {

//    @Value("${aws.region}")
//    private String region;

    private static final String REGION = "eu-north-1";

//    private static final String UBUNTU_AMI_ID = "ami-0c55b159cbfafe1f0";
//    private static final String INSTANCE_TYPE = "t3.micro";
//    private static final String KEY_PAIR_NAME = "MyKeyPair";


//    @Bean
//    public AmazonEC2 ec2Client() {
//        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
//        try {
//            credentialsProvider.getCredentials();
//        } catch (Exception e) {
//            throw new AmazonClientException("Les identifiants IAM ne sont pas localisés dans votre Home Directory. ",e);
//        }
//        return AmazonEC2ClientBuilder.standard()
//            .withCredentials(credentialsProvider)
//            .withRegion(REGION)
//            .build();
//    }
}
