package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DepartmentDetailRestResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.DepartmentCreateResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.DepartmentDetailResponseDto;
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

    @Override
    // TODO: Darius Exception handling?
    public DepartmentCreateResponseDto createDepartment(DepartmentCreateDto dto) throws ConflictException {
        if (departmentRepository.existsByName(dto.getName())) {
            throw new ConflictException("Department with name '" + dto.getName() + "' already exists");
        }

        ApplicationUser supervisor = applicationUserRepository.findById(dto.getSupervisorEmail())
            .orElseThrow(() -> new NotFoundException("Supervisor not found"));

        if (supervisor.getDepartment() != null) {
            throw new ConflictException("Supervisor '" + supervisor.getEmail() + "' is already assigned to a department");
        }

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

        return new DepartmentCreateResponseDto(
            department.getId(),
            department.getName(),
            getSupervisorByDepartmentName(department.getName()).map(UserEmailDto::email).orElse("NONE"));
    }

    @Override
    public DepartmentEditResponseDto editDepartment(DepartmentEditDto dto) {
        Department department = departmentRepository.findByName(dto.getOldName())
            .orElseThrow(() -> new NotFoundException("Department with name '" + dto.getOldName() + "' not found"));

        if (!dto.getOldName().equals(dto.getNewName())
            && departmentRepository.existsByName(dto.getNewName())) {
            throw new ConflictException("Department name '" + dto.getNewName() + "' is already in use");
        }

        ApplicationUser newSupervisor = applicationUserRepository.findById(dto.getSupervisorEmail())
            .orElseThrow(() -> new NotFoundException("User with email '" + dto.getSupervisorEmail() + "' not found"));

        if (newSupervisor.getDepartment() != null
            && !newSupervisor.getDepartment().getName().equals(department.getName())) {
            throw new ConflictException("Supervisor '" + newSupervisor.getEmail()
                + "' is already assigned to another department");
        }

        getSupervisorByDepartmentName(department.getName())
            .filter(oldSup -> !oldSup.email().equals(newSupervisor.getEmail()))
            .ifPresent(oldSupDto -> {
                ApplicationUser oldSupervisor = applicationUserRepository.findById(oldSupDto.email())
                    .orElseThrow(() -> new IllegalStateException("Old supervisor '" + oldSupDto.email() + "' not found in DB"));

                oldSupervisor.setDepartment(null);
                oldSupervisor.getRoles().removeIf(role -> role.getName().equals("SUPERVISOR"));
                applicationUserRepository.save(oldSupervisor);
            });

        department.setName(dto.getNewName());
        departmentRepository.save(department);

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
    public List<DepartmentDetailResponseDto> getAllDepartments() {
        return departmentRepository.findAll().stream()
            .map(dept -> new DepartmentDetailResponseDto(
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
