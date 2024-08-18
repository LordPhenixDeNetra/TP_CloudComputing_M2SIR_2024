package org.netra.aws_project.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class VpcDTO {

    private Long id;

    @Size(max = 255)
    private String vpcId;

//    @NotNull
    @Size(max = 255)
    private String nom;

//    @NotNull
    @Size(max = 255)
    private String vpcCIDR;

//    @NotNull
    @Size(max = 255)
    private String subnetPublicCIDR;

//    @NotNull
    @Size(max = 255)
    private String subnetPrivateCIDR;

//    @NotNull
    @Size(max = 255)
    private String subnetPublicName;

//    @NotNull
    @Size(max = 255)
    private String subnetPrivateName;

//    @NotNull
    @Size(max = 255)
    private String subnetPublicId;

//    @NotNull
    @Size(max = 255)
    private String subnetPrivateId;

//    @NotNull
    @Size(max = 255)
    @JsonProperty("iGatewayeId")
    private String iGatewayeId;

//    @NotNull
    @Size(max = 255)
    private String groupSecPublicName;

//    @NotNull
    @Size(max = 255)
    private String groupSecPrivateName;

//    @NotNull
    @Size(max = 255)
    private String routeTableId;

}
