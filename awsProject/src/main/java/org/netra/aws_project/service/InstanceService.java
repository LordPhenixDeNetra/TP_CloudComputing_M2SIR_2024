package org.netra.aws_project.service;

import java.util.List;
import java.util.Optional;

import org.netra.aws_project.domain.Instance;
import org.netra.aws_project.domain.Vpc;
import org.netra.aws_project.model.InstanceDTO;
import org.netra.aws_project.repos.InstanceRepository;
import org.netra.aws_project.repos.VpcRepository;
import org.netra.aws_project.util.NotFoundException;
import org.netra.aws_project.util.aws.AWSCreateVPC;
import org.netra.aws_project.util.aws.AWSDestroyVPC;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class InstanceService {

    private final InstanceRepository instanceRepository;
    private final VpcRepository vpcRepository;

    private final AWSCreateVPC vpcCreator;
    private final AWSDestroyVPC vpcDestroy;

    public InstanceService(final InstanceRepository instanceRepository,
                           final VpcRepository vpcRepository, AWSCreateVPC vpcCreator, AWSDestroyVPC vpcDestroy) {
        this.instanceRepository = instanceRepository;
        this.vpcRepository = vpcRepository;
        this.vpcCreator = vpcCreator;
        this.vpcDestroy = vpcDestroy;
    }

    public List<InstanceDTO> findAll() {
        final List<Instance> instances = instanceRepository.findAll(Sort.by("id"));
        return instances.stream()
                .map(instance -> mapToDTO(instance, new InstanceDTO()))
                .toList();
    }

    public InstanceDTO get(final Long id) {
        return instanceRepository.findById(id)
                .map(instance -> mapToDTO(instance, new InstanceDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Long create(final InstanceDTO instanceDTO) {

        final Instance instance = new Instance();
        mapToEntity(instanceDTO, instance);
        return instanceRepository.save(instance).getId();
    }

    public Long createWithInstanceType(InstanceDTO instanceDTO) throws InterruptedException {

        System.out.println("===============================");
        System.out.println("==========IS PUBLIC VALUE @@@@@@ : " + instanceDTO.isPublicInstance());
        System.out.println("===============================");

        Vpc vpc = vpcRepository.findById(instanceDTO.getVpc()).get();
        String instanceId;

        if(instanceDTO.isPublicInstance()){
//            if(getPublic){

            String publicSubnetId = vpc.getSubnetPrivateId();
            instanceId = vpcCreator.createInstanceWithoutId(publicSubnetId, instanceDTO.getName(), true);
//            Thread.sleep(5000);
            instanceDTO.setInstanceId(instanceId);

            System.out.println(vpc.getSubnetPublicId());

        }else {

            String privateSubnetId = vpc.getSubnetPrivateId();
            instanceId = vpcCreator.createInstanceWithoutId(privateSubnetId, instanceDTO.getName(), false);
//            Thread.sleep(5000);
            instanceDTO.setInstanceId(instanceId);

            System.out.println(vpc.getSubnetPrivateId());
        }

        final Instance instance = new Instance();
        mapToEntity(instanceDTO, instance);

        System.out.println("Is Publiic :" + instanceDTO.isPublicInstance());
        System.out.println("Intance ID :" + instanceDTO.getInstanceId());
        System.out.println("VPC ID :" + instanceDTO.getVpc());

        instance.setInstanceId(instanceId);

        return instanceRepository.save(instance).getId();

//        return null;
    }

    public Long createPublicInstance(final InstanceDTO instanceDTO) throws InterruptedException {

        Vpc vpc = vpcRepository.findById(instanceDTO.getVpc()).get();

        String publicSubnetId = vpc.getSubnetPrivateId();

        String instanceId = vpcCreator.createInstanceWithoutId(publicSubnetId, instanceDTO.getName(), true);
        Thread.sleep(5000);

        instanceDTO.setInstanceId(instanceId);

        final Instance instance = new Instance();
        mapToEntity(instanceDTO, instance);
        return instanceRepository.save(instance).getId();
    }

    public Long createPrivateInstance(final InstanceDTO instanceDTO) throws InterruptedException {

        Vpc vpc = vpcRepository.findById(instanceDTO.getVpc()).get();

        String privateSubnetId = vpc.getSubnetPrivateId();

        String instanceId = vpcCreator.createInstanceWithoutId(privateSubnetId, instanceDTO.getName(), false);
        Thread.sleep(5000);

        instanceDTO.setInstanceId(instanceId);

        final Instance instance = new Instance();
        mapToEntity(instanceDTO, instance);
        return instanceRepository.save(instance).getId();
    }

    public void update(final Long id, final InstanceDTO instanceDTO) {
        final Instance instance = instanceRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(instanceDTO, instance);
        instanceRepository.save(instance);
    }

    public void delete(final Long id) throws InterruptedException {
        Instance instance = instanceRepository.findById(id).get();
        vpcDestroy.deleteInstance(instance.getInstanceId());
        Thread.sleep(5000);
        instanceRepository.deleteById(id);
    }

    private InstanceDTO mapToDTO(final Instance instance, final InstanceDTO instanceDTO) {
        instanceDTO.setId(instance.getId());
        instanceDTO.setInstanceId(instance.getInstanceId());
        instanceDTO.setName(instance.getName());
        instanceDTO.setVpc(instance.getVpc() == null ? null : instance.getVpc().getId());
        return instanceDTO;
    }

    private Instance mapToEntity(final InstanceDTO instanceDTO, final Instance instance) {
        instance.setInstanceId(instanceDTO.getInstanceId());
        instance.setName(instanceDTO.getName());
        final Vpc vpc = instanceDTO.getVpc() == null ? null : vpcRepository.findById(instanceDTO.getVpc())
                .orElseThrow(() -> new NotFoundException("vpc not found"));
        instance.setVpc(vpc);
        return instance;
    }

}
