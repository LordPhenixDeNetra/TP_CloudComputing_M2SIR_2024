package org.netra.aws_project.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class InstanceDTO {

    private Long id;

    @Size(max = 255)
    private String instanceId;

//    @NotNull
    @Size(max = 255)
    private String name;

    private Long vpc;

    private boolean publicInstance;
}
