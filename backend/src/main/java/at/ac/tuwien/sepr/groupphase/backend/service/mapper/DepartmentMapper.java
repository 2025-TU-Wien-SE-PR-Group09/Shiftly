package at.ac.tuwien.sepr.groupphase.backend.service.mapper;

import at.ac.tuwien.sepr.groupphase.backend.entity.Department;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.department.DepartmentDto;

/**
 * Mapper class for the Department.
 */
public class DepartmentMapper {

    public static DepartmentDto fromEntity(Department department) {
        var mappedPlans = department.getPlans().stream().map(ShiftPlanningMapper.Plans::fromEntity).toList();
        return new DepartmentDto(department.getId(), department.getName(), mappedPlans);
    }
}