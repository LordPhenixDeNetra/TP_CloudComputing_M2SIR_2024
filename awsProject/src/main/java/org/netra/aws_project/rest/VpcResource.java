package org.netra.aws_project.rest;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.netra.aws_project.model.VpcDTO;
import org.netra.aws_project.service.VpcService;
import org.netra.aws_project.util.ReferencedException;
import org.netra.aws_project.util.ReferencedWarning;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value = "/api/vpcs", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin("*")
public class VpcResource {

    private final VpcService vpcService;

    public VpcResource(final VpcService vpcService) {
        this.vpcService = vpcService;
    }

    @GetMapping
    public ResponseEntity<List<VpcDTO>> getAllVpcs() {
        return ResponseEntity.ok(vpcService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VpcDTO> getVpc(@PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(vpcService.get(id));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createVpc(@RequestBody @Valid final VpcDTO vpcDTO){
        final Long createdId;
        try {
            createdId = vpcService.create(vpcDTO);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Long> updateVpc(@PathVariable(name = "id") final Long id,
            @RequestBody @Valid final VpcDTO vpcDTO) {
        vpcService.update(id, vpcDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteVpc(@PathVariable(name = "id") final Long id) {
//        final ReferencedWarning referencedWarning = vpcService.getReferencedWarning(id);
//        if (referencedWarning != null) {
//            throw new ReferencedException(referencedWarning);
//        }
        vpcService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
