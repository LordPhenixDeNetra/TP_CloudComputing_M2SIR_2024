package org.netra.aws_project.service;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.netra.aws_project.domain.Instance;
import org.netra.aws_project.domain.Vpc;
import org.netra.aws_project.model.VpcDTO;
import org.netra.aws_project.repos.InstanceRepository;
import org.netra.aws_project.repos.VpcRepository;
import org.netra.aws_project.util.NotFoundException;
import org.netra.aws_project.util.ReferencedException;
import org.netra.aws_project.util.ReferencedWarning;
import org.netra.aws_project.util.aws.AWSCreateVPC;
import org.netra.aws_project.util.aws.AWSDestroyVPC;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class VpcService {

    private final VpcRepository vpcRepository;
    private final InstanceRepository instanceRepository;

    private final AWSCreateVPC vpcCreator;
    private final AWSDestroyVPC vpcDestroy;

    public VpcService(final VpcRepository vpcRepository,
                      final InstanceRepository instanceRepository, AWSCreateVPC vpcCreator, AWSDestroyVPC vpcDestroy) {
        this.vpcRepository = vpcRepository;
        this.instanceRepository = instanceRepository;
        this.vpcCreator = vpcCreator;
        this.vpcDestroy = vpcDestroy;
    }

    public List<VpcDTO> findAll() {
        final List<Vpc> vpcs = vpcRepository.findAll(Sort.by("id"));
        return vpcs.stream()
                .map(vpc -> mapToDTO(vpc, new VpcDTO()))
                .toList();
    }

    public VpcDTO get(final Long id) {
        return vpcRepository.findById(id)
                .map(vpc -> mapToDTO(vpc, new VpcDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Long create(final VpcDTO vpcDTO) throws InterruptedException {

        String vpcId = vpcCreator.createVPC(vpcDTO.getVpcCIDR(), vpcDTO.getNom());
        Thread.sleep(5000);
        String publicSubnetId = vpcCreator.createSubnet(vpcId, vpcDTO.getSubnetPublicCIDR(), vpcDTO.getSubnetPublicName());
        Thread.sleep(5000);
        String privateSubnetId = vpcCreator.createSubnet(vpcId, vpcDTO.getSubnetPrivateCIDR(), vpcDTO.getSubnetPrivateName());
        Thread.sleep(5000);
        String internetGateway = vpcCreator.createInternetGateway(vpcId);
        Thread.sleep(5000);
        String routableId = vpcCreator.createRouteTable(publicSubnetId, internetGateway, vpcId, true);
        Thread.sleep(5000);

        final Vpc vpc = new Vpc();
        mapToEntity(vpcDTO, vpc);

        vpc.setVpcId(vpcId);
        vpc.setSubnetPublicId(publicSubnetId);
        vpc.setSubnetPrivateId(privateSubnetId);
        vpc.setIGatewayeId(internetGateway);
        vpc.setRouteTableId(routableId);
        return vpcRepository.save(vpc).getId();

//        return null;
    }

    public void update(final Long id, final VpcDTO vpcDTO) {
        final Vpc vpc = vpcRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(vpcDTO, vpc);
        vpcRepository.save(vpc);
    }

    public void delete(final Long id) {

        Vpc vpc = vpcRepository.findById(id).get();
//        Instance instance = instanceRepository.findFirstByVpc(vpc);
        List<Instance> instanceList = instanceRepository.findAllByVpc(vpc);

        String instanceRemovable = "";

        if (instanceList != null && !instanceList.isEmpty()){
            for (int i = 0; i < instanceList.size(); i++) {
                instanceRemovable = instanceList.get(i).getInstanceId();
                instanceRepository.delete(instanceList.get(i));
            }
        }else {
            final ReferencedWarning referencedWarning = getReferencedWarning(id);
            if (referencedWarning != null) {
                throw new ReferencedException(referencedWarning);
            }
        }

//        if (instance != null){
//            instanceRemovable = instance.getInstanceId();
//            instanceRepository.delete(instance);
//        }else {
//            final ReferencedWarning referencedWarning = getReferencedWarning(id);
//            if (referencedWarning != null) {
//                throw new ReferencedException(referencedWarning);
//            }
//        }

        List<String> subnetIds = new ArrayList<>();
        subnetIds.add(vpc.getSubnetPublicId());
        subnetIds.add(vpc.getSubnetPrivateId());

        vpcDestroy.deleteAllResources(
            instanceRemovable,
            vpc.getRouteTableId(),
            subnetIds,
            vpc.getIGatewayeId(),
            vpc.getVpcId()
        );

        vpcRepository.deleteById(id);
    }

    private VpcDTO mapToDTO(final Vpc vpc, final VpcDTO vpcDTO) {
        vpcDTO.setId(vpc.getId());
        vpcDTO.setVpcId(vpc.getVpcId());
        vpcDTO.setNom(vpc.getNom());
        vpcDTO.setVpcCIDR(vpc.getVpcCIDR());
        vpcDTO.setSubnetPublicCIDR(vpc.getSubnetPublicCIDR());
        vpcDTO.setSubnetPrivateCIDR(vpc.getSubnetPrivateCIDR());
        vpcDTO.setSubnetPublicName(vpc.getSubnetPublicName());
        vpcDTO.setSubnetPrivateName(vpc.getSubnetPrivateName());
        vpcDTO.setIGatewayeId(vpc.getIGatewayeId());
        vpcDTO.setGroupSecPublicName(vpc.getGroupSecPublicName());
        vpcDTO.setGroupSecPrivateName(vpc.getGroupSecPrivateName());
        return vpcDTO;
    }

    private Vpc mapToEntity(final VpcDTO vpcDTO, final Vpc vpc) {
        vpc.setVpcId(vpcDTO.getVpcId());
        vpc.setNom(vpcDTO.getNom());
        vpc.setVpcCIDR(vpcDTO.getVpcCIDR());
        vpc.setSubnetPublicCIDR(vpcDTO.getSubnetPublicCIDR());
        vpc.setSubnetPrivateCIDR(vpcDTO.getSubnetPrivateCIDR());
        vpc.setSubnetPublicName(vpcDTO.getSubnetPublicName());
        vpc.setSubnetPrivateName(vpcDTO.getSubnetPrivateName());
        vpc.setIGatewayeId(vpcDTO.getIGatewayeId());
        vpc.setGroupSecPublicName(vpcDTO.getGroupSecPublicName());
        vpc.setGroupSecPrivateName(vpcDTO.getGroupSecPrivateName());
        return vpc;
    }

    public ReferencedWarning getReferencedWarning(final Long id) {
        final ReferencedWarning referencedWarning = new ReferencedWarning();
        final Vpc vpc = vpcRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        final Instance vpcInstance = instanceRepository.findFirstByVpc(vpc);
        if (vpcInstance != null) {
            referencedWarning.setKey("vpc.instance.vpc.referenced");
            referencedWarning.addParam(vpcInstance.getId());
            return referencedWarning;
        }
        return null;
    }

}
