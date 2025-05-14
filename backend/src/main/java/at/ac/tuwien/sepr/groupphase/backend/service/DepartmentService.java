package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.entity.Department;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.DepartmentCreateDto;

public interface DepartmentService {

    Department createDepartment(DepartmentCreateDto dto);
}
