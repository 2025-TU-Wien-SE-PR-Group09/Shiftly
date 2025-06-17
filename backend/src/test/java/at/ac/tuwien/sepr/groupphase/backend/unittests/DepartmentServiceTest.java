package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Department;
import at.ac.tuwien.sepr.groupphase.backend.entity.ScheduledShift;
import at.ac.tuwien.sepr.groupphase.backend.entity.ScheduledShiftAssignment;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.DepartmentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.department.DepartmentCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.department.DepartmentCreateResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.department.DepartmentEditResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.employee.EmployeeDto;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.DepartmentServiceImpl;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.department.DepartmentEditDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static at.ac.tuwien.sepr.groupphase.backend.basetest.TestData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DepartmentServiceTest {

    private DepartmentServiceImpl departmentService;

    private DepartmentRepository departmentRepository;
    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        departmentRepository = mock(DepartmentRepository.class);
        userRepository = mock(UserRepository.class);
        userService = mock(UserService.class);

        departmentService = new DepartmentServiceImpl(
            departmentRepository,
            userRepository,
            userService
        );
    }

    @Test
    void createDepartment_supervisorAlreadyAssigned_shouldThrowConflictException() {
        ApplicationUser supervisor = new ApplicationUser();
        supervisor.setEmail(SUPERVISOR_EMAIL);
        supervisor.setDepartment(new Department()); // Supervisor already assigned

        DepartmentCreateDto dto = new DepartmentCreateDto(DEPARTMENT_NAME, SUPERVISOR_EMAIL);

        when(departmentRepository.existsByName(DEPARTMENT_NAME)).thenReturn(false);
        when(userRepository.findById(SUPERVISOR_EMAIL)).thenReturn(Optional.of(supervisor));

        ConflictException ex = assertThrows(
            ConflictException.class,
            () -> departmentService.createDepartment(dto)
        );

        assertEquals("Supervisor '" + SUPERVISOR_EMAIL + "' is already assigned to a department", ex.getMessage());
    }

    @Test
    void editDepartment_assignSupervisorAlreadyAssignedToOtherDepartment_throwsConflict() {
        Department currentDept = new Department();
        currentDept.setName(DEPARTMENT_NAME);

        Department otherDept = new Department();
        otherDept.setName("OtherDept");

        ApplicationUser supervisor = new ApplicationUser();
        supervisor.setEmail(SUPERVISOR_EMAIL);
        supervisor.setDepartment(otherDept); // Already assigned elsewhere

        DepartmentEditDto dto = new DepartmentEditDto(DEPARTMENT_NAME, "UpdatedName", SUPERVISOR_EMAIL);

        when(departmentRepository.findByName(DEPARTMENT_NAME)).thenReturn(Optional.of(currentDept));
        when(userRepository.findById(SUPERVISOR_EMAIL)).thenReturn(Optional.of(supervisor));

        ConflictException ex = assertThrows(
            ConflictException.class,
            () -> departmentService.editDepartment(dto)
        );

        assertEquals("Supervisor '" + SUPERVISOR_EMAIL + "' is already assigned to another department", ex.getMessage());
    }

    @Test
    void editDepartment_duplicateDepartmentName_throwsConflict() {
        Department currentDept = new Department();
        currentDept.setName(DEPARTMENT_NAME);

        ApplicationUser supervisor = new ApplicationUser();
        supervisor.setEmail(SUPERVISOR_EMAIL);

        DepartmentEditDto dto = new DepartmentEditDto(DEPARTMENT_NAME, "ExistingDept", SUPERVISOR_EMAIL);

        when(departmentRepository.findByName(DEPARTMENT_NAME)).thenReturn(Optional.of(currentDept));
        when(departmentRepository.existsByName("ExistingDept")).thenReturn(true);

        ConflictException ex = assertThrows(
            ConflictException.class,
            () -> departmentService.editDepartment(dto)
        );

        assertEquals("Department name 'ExistingDept' is already in use", ex.getMessage());
    }

    @Test
    void createDepartment_validSupervisor_createsSuccessfully() {
        ApplicationUser supervisor = new ApplicationUser();
        supervisor.setEmail(SUPERVISOR_EMAIL);
        supervisor.setDepartment(null);

        Department department = new Department();
        department.setName(DEPARTMENT_NAME);

        DepartmentCreateDto dto = new DepartmentCreateDto(DEPARTMENT_NAME, SUPERVISOR_EMAIL);

        when(departmentRepository.existsByName(DEPARTMENT_NAME)).thenReturn(false);
        when(userRepository.findById(SUPERVISOR_EMAIL)).thenReturn(Optional.of(supervisor));
        when(departmentRepository.save(any(Department.class))).thenAnswer(inv -> {
            Department d = inv.getArgument(0);
            return d;
        });

        DepartmentCreateResponseDto result = departmentService.createDepartment(dto);

        assertAll(
            () -> assertNotNull(result),
            () -> assertEquals(DEPARTMENT_NAME, result.getName()),
            () -> assertEquals("NONE", result.getSupervisorEmail())
        );
    }

    @Test
    void editDepartment_changeSupervisorToValidOne_updatesSuccessfully() {
        Department currentDept = new Department();
        currentDept.setName(DEPARTMENT_NAME);

        ApplicationUser newSupervisor = new ApplicationUser();
        newSupervisor.setEmail("new@shyft.local");
        newSupervisor.setDepartment(null);

        ApplicationUser oldSupervisor = new ApplicationUser();
        oldSupervisor.setEmail(SUPERVISOR_EMAIL);
        oldSupervisor.setDepartment(currentDept);
        oldSupervisor.getRoles().add(new ApplicationRole("SUPERVISOR"));

        DepartmentEditDto dto = new DepartmentEditDto(DEPARTMENT_NAME, DEPARTMENT_NAME, "new@shyft.local");

        when(departmentRepository.findByName(DEPARTMENT_NAME)).thenReturn(Optional.of(currentDept));
        when(userRepository.findById("new@shyft.local")).thenReturn(Optional.of(newSupervisor));
        when(userRepository.findById(SUPERVISOR_EMAIL)).thenReturn(Optional.of(oldSupervisor));
        when(userRepository.findAll()).thenReturn(List.of(oldSupervisor));
        when(userRepository.findById(SUPERVISOR_EMAIL)).thenReturn(Optional.of(oldSupervisor));

        DepartmentEditResponseDto result = departmentService.editDepartment(dto);

        assertAll(
            () -> assertNotNull(result),
            () -> assertEquals(DEPARTMENT_NAME, result.getName()),
            () -> assertEquals("new@shyft.local", result.getSupervisorEmail())
        );
    }

    @Test
    void removeEmployeeFromDepartment_validEmployee_removesSuccessfully() {
        // Arrange
        String email = "employee@shyft.local";
        String departmentName = "TestDepartment";

        Department department = new Department();
        department.setName(departmentName);

        ApplicationUser employee = new ApplicationUser();
        employee.setEmail(email);
        employee.setDepartment(department);
        employee.getRoles().add(new ApplicationRole("EMPLOYEE"));

        EmployeeDto employeeDto = new EmployeeDto(email, departmentName);

        when(userRepository.findById(email)).thenReturn(Optional.of(employee));
        when(departmentRepository.findByName(departmentName)).thenReturn(Optional.of(department));

        // Act
        departmentService.removeEmployeeFromDepartment(employeeDto);

        // Assert
        assertAll(
            () -> assertNull(employee.getDepartment(), "Department should be null after removal"),
            () -> assertTrue(
                employee.getRoles().stream().noneMatch(r -> r.getName().equals("EMPLOYEE")),
                "Employee role should be removed"
            )
        );
    }

    @Test
    void removeEmployeeFromDepartment_userNotFound_throwsNotFoundException() {
        // Arrange
        String email = "nonexistent@shyft.local";
        String departmentName = "TestDepartment";
        EmployeeDto employeeDto = new EmployeeDto(email, departmentName);

        when(userRepository.findById(email)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> departmentService.removeEmployeeFromDepartment(employeeDto)
        );

        assertEquals("User with email '" + email + "' not found", exception.getMessage());
    }

    @Test
    void removeEmployeeFromDepartment_removesAssignmentsAndDepartment() {
        // Arrange
        String email = "employee@shyft.local";
        String departmentName = "TestDepartment";

        Department department = new Department();
        department.setName(departmentName);

        ApplicationUser employee = new ApplicationUser();
        employee.setEmail(email);
        employee.setDepartment(department);
        employee.getRoles().add(new ApplicationRole("EMPLOYEE"));

        // Setup fake assignments
        ScheduledShift shift1 = new ScheduledShift();
        shift1.setId(1L);
        ScheduledShift shift2 = new ScheduledShift();
        shift2.setId(2L);

        ScheduledShiftAssignment assignment1 = new ScheduledShiftAssignment(shift1, employee);
        ScheduledShiftAssignment assignment2 = new ScheduledShiftAssignment(shift2, employee);

        // Füge sie der Collection hinzu (nur wenn du ein Mapping hast!)
        employee.getAssignments().addAll(List.of(assignment1, assignment2));

        when(userRepository.findById(email)).thenReturn(Optional.of(employee));
        when(departmentRepository.findByName(departmentName)).thenReturn(Optional.of(department));

        // Act - vor dem Entfernen prüfen
        assertEquals(2, employee.getAssignments().size(), "User sollte 2 Assignments haben");

        EmployeeDto employeeDto = new EmployeeDto(email, departmentName);
        departmentService.removeEmployeeFromDepartment(employeeDto);

        // Assert - nach dem Entfernen prüfen
        assertNull(employee.getDepartment(), "Department sollte null sein");
        assertTrue(employee.getRoles().stream().noneMatch(r -> r.getName().equals("EMPLOYEE")), "Employee Rolle sollte entfernt sein");
        assertEquals(0, employee.getAssignments().size(), "User sollte keine Assignments mehr haben");
    }

}