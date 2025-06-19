package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Department;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.DepartmentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.EmployeeService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.department.DepartmentNameDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.employee.EmployeeDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.employee.EmployeeListItemDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.Role;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.employee.JumperDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.UserRoleDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Set;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final UserService userService;

    public EmployeeServiceImpl(UserRepository userRepository,
                               UserService userService,
                               DepartmentRepository departmentRepository) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.departmentRepository = departmentRepository;
    }

    @Override
    public EmployeeDto convertUserToEmployee(EmployeeDto employeeDto) throws ConflictException, NotFoundException {
        LOGGER.trace("convertUserToEmployee({})", employeeDto);

        ApplicationUser applicationUser = userRepository.findByEmail(employeeDto.email())
            .orElseThrow(() -> new NotFoundException("Employee with email " + employeeDto.email() + " not found"));

        if (applicationUser.getRoles().stream()
            .map(ApplicationRole::getName)
            .anyMatch(r -> r.equals("EMPLOYEE") || r.equals("JUMPER") || r.equals("ADMIN") || r.equals("SUPERVISOR"))) {
            throw new ConflictException(
                "Employee with email " + employeeDto.email() + " is already member of a department or admin.");
        }

        userService.assignRoleToUser(new UserRoleDto(applicationUser.getEmail(), Role.EMPLOYEE, employeeDto.departmentName()));

        Department department = departmentRepository.findById(employeeDto.departmentName())
            .orElseThrow(
                () -> new NotFoundException("Department with name " + employeeDto.departmentName() + " not found"));

        applicationUser.setDepartment(department);
        userRepository.save(applicationUser);

        return employeeDto;
    }

    @Override
    public JumperDto convertUserToJumper(JumperDto jumperDto) throws ConflictException, NotFoundException {
        LOGGER.trace("convertUserToEmployee({})", jumperDto);

        ApplicationUser applicationUser = userRepository.findByEmail(jumperDto.email())
            .orElseThrow(() -> new NotFoundException("Employee with email " + jumperDto.email() + " not found"));

        if (applicationUser.getRoles().stream()
            .map(ApplicationRole::getName)
            .anyMatch(r -> r.equals("EMPLOYEE") || r.equals("JUMPER") || r.equals("ADMIN") || r.equals("SUPERVISOR"))) {
            throw new ConflictException(
                "Employee with email " + jumperDto.email() + " is already member of a department or admin.");
        }

        userService.assignRoleToUser(new UserRoleDto(applicationUser.getEmail(), Role.JUMPER, jumperDto.departmentName()));

        Department department = departmentRepository.findById(jumperDto.departmentName())
            .orElseThrow(
                () -> new NotFoundException("Department with name " + jumperDto.departmentName() + " not found"));

        applicationUser.setDepartment(department);
        userRepository.save(applicationUser);

        return jumperDto;
    }

    @Override
    public List<EmployeeListItemDto> getEmployeesOfDepartment(DepartmentNameDto departmentName) {
        LOGGER.trace("getEmployeesOfDepartment({})", departmentName);

        Department department = departmentRepository.findByName(departmentName.name())
            .orElseThrow(
                () -> new NotFoundException("Department with name " + departmentName.name() + " not found"));

        Set<ApplicationUser> employees = department.getUsers();

        if (employees != null) {
            return employees.stream()
                .map(e -> new EmployeeListItemDto(
                    e.getEmail(),
                    e.getFirstName(),
                    e.getLastName(),
                    e.getRoles().stream().map(ApplicationRole::getName).findFirst().orElse("None")))
                .toList();
        }

        return List.of();
    }
}
