package org.netra.aws_project.repos;

import org.netra.aws_project.domain.Instance;
import org.netra.aws_project.domain.Vpc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface InstanceRepository extends JpaRepository<Instance, Long> {

    Instance findFirstByVpc(Vpc vpc);
    List<Instance> findAllByVpc(Vpc vpc);

}
