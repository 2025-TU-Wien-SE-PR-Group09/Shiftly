package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DepartmentDetailRestDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Department;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.repository.DepartmentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.DepartmentService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.DepartmentCreateDto;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository applicationUserRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository,
                                 UserRepository applicationUserRepository) {
        this.departmentRepository = departmentRepository;
        this.applicationUserRepository = applicationUserRepository;
    }

    @Override
    public Department createDepartment(DepartmentCreateDto dto) {
        if (departmentRepository.existsByName(dto.getName())) {
            throw new ConflictException("Department with name '" + dto.getName() + "' already exists");
        }

        ApplicationUser supervisor = applicationUserRepository.findById(dto.getSupervisorEmail())
            .orElseThrow(() -> new EntityNotFoundException("Supervisor not found"));

        Department department = new Department();
        department.setName(dto.getName());
        department.setSupervisor(supervisor);

        return departmentRepository.save(department);
    }

    @Override
    public List<DepartmentDetailRestDto> getAllDepartments() {
        return departmentRepository.findAll().stream()
            .map(dept -> new DepartmentDetailRestDto(
                dept.getId(),
                dept.getName(),
                dept.getSupervisor().getEmail()
            ))
            .toList();
    }
}
