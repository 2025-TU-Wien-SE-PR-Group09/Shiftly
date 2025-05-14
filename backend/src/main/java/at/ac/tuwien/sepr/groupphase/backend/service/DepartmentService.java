package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DepartmentDetailRestDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Department;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.DepartmentCreateDto;

import java.util.List;

public interface DepartmentService {

    Department createDepartment(DepartmentCreateDto dto);

    List<DepartmentDetailRestDto> getAllDepartments();
}
