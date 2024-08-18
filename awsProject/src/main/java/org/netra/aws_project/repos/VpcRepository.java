package org.netra.aws_project.repos;

import org.netra.aws_project.domain.Vpc;
import org.springframework.data.jpa.repository.JpaRepository;


public interface VpcRepository extends JpaRepository<Vpc, Long> {
}
