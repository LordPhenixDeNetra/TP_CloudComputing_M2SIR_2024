package org.netra.aws_project.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import java.time.OffsetDateTime;
import java.util.Set;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Vpc {

    @Id
    @Column(nullable = false, updatable = false)
    @SequenceGenerator(
            name = "primary_sequence",
            sequenceName = "primary_sequence",
            allocationSize = 1,
            initialValue = 10000
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "primary_sequence"
    )
    private Long id;

    @Column
    private String vpcId;

//    @Column(nullable = false)
    private String nom;

//    @Column(nullable = false)
    private String vpcCIDR;

//    @Column(nullable = false)
    private String subnetPublicCIDR;

//    @Column(nullable = false)
    private String subnetPrivateCIDR;

//    @Column(nullable = false)
    private String subnetPublicName;

//    @Column(nullable = false)
    private String subnetPrivateName;

//    @Column(nullable = false)
    private String subnetPublicId;

//    @Column(nullable = false)
    private String subnetPrivateId;

//    @Column(nullable = false)
    private String iGatewayeId;

//    @Column(nullable = false)
    private String groupSecPublicName;

//    @Column(nullable = false)
    private String groupSecPrivateName;

//    @Column(nullable = false)
    private String routeTableId;

    @OneToMany(mappedBy = "vpc")
    private Set<Instance> instances;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @LastModifiedDate
    @Column(nullable = false)
    private OffsetDateTime lastUpdated;

    @Override
    public String toString() {
        return "Vpc{" +
            "id=" + id +
            ", vpcId='" + vpcId + '\'' +
            ", nom='" + nom + '\'' +
            ", vpcCIDR='" + vpcCIDR + '\'' +
            ", subnetPublicCIDR='" + subnetPublicCIDR + '\'' +
            ", subnetPrivateCIDR='" + subnetPrivateCIDR + '\'' +
            ", subnetPublicName='" + subnetPublicName + '\'' +
            ", subnetPrivateName='" + subnetPrivateName + '\'' +
            ", subnetPublicId='" + subnetPublicId + '\'' +
            ", subnetPrivateId='" + subnetPrivateId + '\'' +
            ", iGatewayeId='" + iGatewayeId + '\'' +
            ", groupSecPublicName='" + groupSecPublicName + '\'' +
            ", groupSecPrivateName='" + groupSecPrivateName + '\'' +
            ", routeTableId='" + routeTableId + '\'' +
            ", instances=" + instances +
            ", dateCreated=" + dateCreated +
            ", lastUpdated=" + lastUpdated +
            '}';
    }
}
