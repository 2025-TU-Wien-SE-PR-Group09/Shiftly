package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Department;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.repository.DepartmentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.department.DepartmentCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.department.DepartmentCreateResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.department.DepartmentEditResponseDto;
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
        department.setId(ID);
        department.setName(DEPARTMENT_NAME);

        DepartmentCreateDto dto = new DepartmentCreateDto(DEPARTMENT_NAME, SUPERVISOR_EMAIL);

        when(departmentRepository.existsByName(DEPARTMENT_NAME)).thenReturn(false);
        when(userRepository.findById(SUPERVISOR_EMAIL)).thenReturn(Optional.of(supervisor));
        when(departmentRepository.save(any(Department.class))).thenAnswer(inv -> {
            Department d = inv.getArgument(0);
            d.setId(ID);
            return d;
        });

        DepartmentCreateResponseDto result = departmentService.createDepartment(dto);

        assertAll(
            () -> assertNotNull(result),
            () -> assertEquals(ID, result.getId()),
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

}