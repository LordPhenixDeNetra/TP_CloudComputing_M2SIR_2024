package org.netra.aws_project;

import com.amazonaws.services.ec2.AmazonEC2;
import org.netra.aws_project.util.aws.AWSDestroyVPC;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;


@SpringBootApplication
public class AwsProjectApplication {

    private final AWSDestroyVPC vpcDestroy;

    public AwsProjectApplication(AWSDestroyVPC vpcDestroy) {
        this.vpcDestroy = vpcDestroy;
    }

    public static void main(final String[] args) {
        SpringApplication.run(AwsProjectApplication.class, args);
    }

    @Bean
    public CommandLineRunner runner(){
        return args -> {
//            vpcDestroy.routeTableExist("rtb-031aafedb4145f363");
//            vpcDestroy.subnetExist("subnet-0bd4a403c72467c79");
//            vpcDestroy.internetGatewayExist("igw-0e0e994522e762b1a");
        };
    }

}
