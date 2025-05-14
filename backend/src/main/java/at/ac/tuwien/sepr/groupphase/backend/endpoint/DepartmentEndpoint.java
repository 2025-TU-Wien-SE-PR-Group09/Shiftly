package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DepartmentCreateRestDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Department;
import at.ac.tuwien.sepr.groupphase.backend.service.DepartmentService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.DepartmentCreateDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/departments")
public class DepartmentEndpoint {

    private final DepartmentService departmentService;

    public DepartmentEndpoint(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    public ResponseEntity<Void> createDepartment(@RequestBody @Valid DepartmentCreateRestDto restDto) {
        DepartmentCreateDto serviceDto = new DepartmentCreateDto(
            restDto.getName(),
            restDto.getSupervisorEmail()
        );
        departmentService.createDepartment(serviceDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
