package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DepartmentCreateRestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DepartmentDetailRestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PlanBlueprintResponse;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.service.DepartmentService;
import at.ac.tuwien.sepr.groupphase.backend.service.ShiftPlanningService;
import at.ac.tuwien.sepr.groupphase.backend.service.mapper.ShiftPlanningMapper;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.DepartmentCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.DepartmentDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Department")
@RestController
@RequestMapping("/api/departments")
public class DepartmentEndpoint {

    private final DepartmentService departmentService;
    private final ShiftPlanningService shiftPlanningService;

    public DepartmentEndpoint(DepartmentService departmentService, ShiftPlanningService shiftPlanningService) {
        this.departmentService = departmentService;
        this.shiftPlanningService = shiftPlanningService;
    }

    @RolesAllowed({"ADMIN"})
    @Operation(summary = "Get all departments")
    @ApiResponse(responseCode = "200", description = "List of all departments")
    @GetMapping
    public List<DepartmentDetailRestDto> getAllDepartments() {
        return departmentService.getAllDepartments();
    }

    @Transactional
    @RolesAllowed({"ADMIN"})
    @Operation(summary = "Create a new department")
    @ApiResponse(responseCode = "201", description = "New department created")
    @PostMapping
    public ResponseEntity<Void> createDepartment(@RequestBody @Valid DepartmentCreateRestDto restDto) {
        DepartmentCreateDto serviceDto = new DepartmentCreateDto(
            restDto.getName(),
            restDto.getSupervisorEmail());
        departmentService.createDepartment(serviceDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Transactional
    @RolesAllowed({"ADMIN"})
    @Operation(summary = "Get shift plan for a department")
    @ApiResponse(responseCode = "200", description = "Shiftplan for the department")
    @GetMapping("/{departmentName}/shiftplanBlueprint")
    public List<PlanBlueprintResponse> getShiftplanBlueprints(
        @PathVariable(name = "departmentName") String departmentName) {

        DepartmentDto department = departmentService.getDepartmentByName(departmentName)
            .orElseThrow(() -> new NotFoundException("Department not found!"));

        return department.plans().stream().map(ShiftPlanningMapper.Plans::toResponse).toList();
    }

}