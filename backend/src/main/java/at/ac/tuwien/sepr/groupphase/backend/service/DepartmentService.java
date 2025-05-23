package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DepartmentDetailRestDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.DepartmentCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.DepartmentDto;

import java.util.List;
import java.util.Optional;

public interface DepartmentService {

    /**
     * Creates a new department.
     *
     * @param dto the department data transfer object containing the details of the department to be created
     * @return the details of the created department
     * @throws ConflictException if a department with the same name already exists
     */
    DepartmentDetailRestDto createDepartment(DepartmentCreateDto dto) throws ConflictException;

    /**
     * Retrieves all departments.
     *
     * @return a list of department details
     */
    List<DepartmentDetailRestDto> getAllDepartments();

    /**
     * Retrieves a department by its name.
     *
     * @param departmentName the name of the department to be retrieved
     * @return an optional containing the department details if found, or empty if not found
     */
    Optional<DepartmentDto> getDepartmentByName(String departmentName);
}
