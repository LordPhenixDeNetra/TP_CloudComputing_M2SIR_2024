package org.netra.aws_project.rest;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.netra.aws_project.domain.Vpc;
import org.netra.aws_project.model.InstanceDTO;
import org.netra.aws_project.repos.VpcRepository;
import org.netra.aws_project.service.InstanceService;
import org.netra.aws_project.util.CustomCollectors;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value = "/api/instances", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin("*")
public class InstanceResource {

    private final InstanceService instanceService;
    private final VpcRepository vpcRepository;

    public InstanceResource(final InstanceService instanceService,
            final VpcRepository vpcRepository) {
        this.instanceService = instanceService;
        this.vpcRepository = vpcRepository;
    }

    @GetMapping
    public ResponseEntity<List<InstanceDTO>> getAllInstances() {
        return ResponseEntity.ok(instanceService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InstanceDTO> getInstance(@PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(instanceService.get(id));
    }

    @PostMapping("/V1")
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createInstance(@RequestBody @Valid final InstanceDTO instanceDTO) {
        final Long createdId = instanceService.create(instanceDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createWithInstanceType(@RequestBody InstanceDTO instanceDTO) {
        final Long createdId;
        try {
            createdId = instanceService.createWithInstanceType(instanceDTO);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }



    @PutMapping("/{id}")
    public ResponseEntity<Long> updateInstance(@PathVariable(name = "id") final Long id,
            @RequestBody @Valid final InstanceDTO instanceDTO) {
        instanceService.update(id, instanceDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteInstance(@PathVariable(name = "id") final Long id) {
        try {
            instanceService.delete(id);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/vpcValues")
    public ResponseEntity<Map<Long, String>> getVpcValues() {
        return ResponseEntity.ok(vpcRepository.findAll(Sort.by("id"))
                .stream()
                .collect(CustomCollectors.toSortedMap(Vpc::getId, Vpc::getNom)));
    }

}
