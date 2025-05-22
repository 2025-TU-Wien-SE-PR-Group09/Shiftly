package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.DepartmentNameDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.EmployeeDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.EmployeeListItemDto;

import java.util.List;

public interface EmployeeService {

    /**
     * Converts an ApplicationUser to an Employee.
     *
     * @param employeeDto a {@link EmployeeDto} containing the user email and the department
     * @return the e-mail of the Employee
     * @throws ConflictException if the user is already an employee, supervisor or admin
     * @throws NotFoundException if the user with the given e-mail address does not exist
     */
    EmployeeDto convertUserToEmployee(EmployeeDto employeeDto) throws ConflictException, NotFoundException;

    /**
     * Lists all employees of a department.
     *
     * @param departmentNameDto a {@link DepartmentNameDto} containing the name of the department
     * @return List of {@link EmployeeListItemDto} containing data about the employees
     */
    List<EmployeeListItemDto> getEmployeesOfDepartment(DepartmentNameDto departmentNameDto);
}
