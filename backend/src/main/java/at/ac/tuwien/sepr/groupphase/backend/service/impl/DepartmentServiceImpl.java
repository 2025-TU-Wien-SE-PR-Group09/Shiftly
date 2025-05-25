package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DepartmentDetailRestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.DepartmentEditDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.DepartmentEditResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Department;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.DepartmentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.DepartmentService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.DepartmentCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.DepartmentDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.Role;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.UserEmailDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.UserRoleDto;
import at.ac.tuwien.sepr.groupphase.backend.service.mapper.DepartmentMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository applicationUserRepository;

    private final UserService userService;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository,
                                 UserRepository applicationUserRepository, UserService userService) {
        this.departmentRepository = departmentRepository;
        this.applicationUserRepository = applicationUserRepository;
        this.userService = userService;
    }

    // Todo return service dto not rest
    @Override
    public DepartmentDetailRestDto createDepartment(DepartmentCreateDto dto) throws ConflictException {
        if (departmentRepository.existsByName(dto.getName())) {
            throw new ConflictException("Department with name '" + dto.getName() + "' already exists");
        }

        ApplicationUser supervisor = applicationUserRepository.findById(dto.getSupervisorEmail())
            .orElseThrow(() -> new NotFoundException("Supervisor not found"));

        Department department = new Department();
        department.setName(dto.getName());
        department = departmentRepository.save(department);

        supervisor.getRoles().clear();
        supervisor.setDepartment(department);
        applicationUserRepository.save(supervisor);
        userService.assignRoleToUser(new UserRoleDto(
            supervisor.getEmail(),
            Role.SUPERVISOR
        ));

        return new DepartmentDetailRestDto(
            department.getId(),
            department.getName(),
            getSupervisorByDepartmentName(department.getName()).map(UserEmailDto::email).orElse("NONE"));
    }

    @Override
    public DepartmentEditResponseDto editDepartment(DepartmentEditDto dto) {
        Department department = departmentRepository.findByName(dto.getName())
            .orElseThrow(() -> new NotFoundException("Department with name '" + dto.getName() + "' not found"));

        ApplicationUser newSupervisor = applicationUserRepository.findById(dto.getSupervisorEmail())
            .orElseThrow(() -> new NotFoundException("User with email '" + dto.getSupervisorEmail() + "' not found"));

        Optional<UserEmailDto> optionalOldSup = getSupervisorByDepartmentName(department.getName());

        if (optionalOldSup.isPresent() && !optionalOldSup.get().email().equals(newSupervisor.getEmail())) {
            ApplicationUser oldSupervisor = applicationUserRepository.findById(optionalOldSup.get().email())
                .orElseThrow(() -> new IllegalStateException("Expected old supervisor not found in DB"));

            oldSupervisor.setDepartment(null);
            oldSupervisor.getRoles().removeIf(role -> role.getName().equals("SUPERVISOR"));
            applicationUserRepository.save(oldSupervisor);
        }

        newSupervisor.setDepartment(department);
        newSupervisor.getRoles().clear();
        applicationUserRepository.save(newSupervisor);

        userService.assignRoleToUser(new UserRoleDto(
            newSupervisor.getEmail(),
            Role.SUPERVISOR
        ));

        return new DepartmentEditResponseDto(
            department.getName(),
            newSupervisor.getEmail()
        );
    }

    @Override
    public List<DepartmentDetailRestDto> getAllDepartments() {
        return departmentRepository.findAll().stream()
            .map(dept -> new DepartmentDetailRestDto(
                dept.getId(),
                dept.getName(),
                getSupervisorByDepartmentName(dept.getName()).map(UserEmailDto::email).orElse("NONE")))
            .toList();
    }

    @Override
    public Optional<DepartmentDto> getDepartmentByName(String departmentName) {
        return departmentRepository.findByName(departmentName)
            .map(DepartmentMapper::fromEntity);
    }

    @Override
    public Optional<UserEmailDto> getSupervisorByDepartmentName(String departmentName) {
        Optional<Department> actDept = departmentRepository.findByName(departmentName);

        if (actDept.isPresent()) {
            Department department = actDept.get();
            Set<ApplicationUser> users = department.getUsers();
            return users.stream()
                .filter(u -> u.getRoles()
                    .stream()
                    .map(ApplicationRole::getName)
                    .anyMatch(r -> r.equals("SUPERVISOR")))
                .findFirst().map(applicationUser -> new UserEmailDto(applicationUser.getEmail()));
        }

        return Optional.empty();
    }
}
