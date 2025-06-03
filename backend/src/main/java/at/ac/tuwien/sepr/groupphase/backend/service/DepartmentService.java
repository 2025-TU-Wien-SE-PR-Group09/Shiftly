package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.service.dto.department.DepartmentCreateResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.department.DepartmentDetailResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.department.DepartmentEditDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.department.DepartmentEditResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.department.DepartmentCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.department.DepartmentDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.UserEmailDto;

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
    DepartmentCreateResponseDto createDepartment(DepartmentCreateDto dto) throws ConflictException;

    /**
     * Edits an existing department.
     *
     * @param dto the department edit data transfer object containing the details of the department to be edited
     * @return the response containing the details of the edited department
     * @throws NotFoundException if the department to be edited does not exist
     */
    DepartmentEditResponseDto editDepartment(DepartmentEditDto dto) throws NotFoundException;

    /**
     * Retrieves all departments.
     *
     * @return a list of department details
     */
    List<DepartmentDetailResponseDto> getAllDepartments();

    /**
     * Retrieves a department by its name.
     *
     * @param departmentName the name of the department to be retrieved
     * @return an optional containing the department details if found, or empty if not found
     */
    Optional<DepartmentDto> getDepartmentByName(String departmentName);

    /**
     * Retrieves the supervisor of a department by the department name.
     *
     * @param departmentName the name of the department
     * @return an optional containing the supervisor details if found, or empty if not found
     */
    Optional<UserEmailDto> getSupervisorByDepartmentName(String departmentName);
}
