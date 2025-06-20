package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.ConcreteShiftPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConcreteShiftPlanRepository extends JpaRepository<ConcreteShiftPlan, Long> {
    List<ConcreteShiftPlan> findByDepartmentName(String departmentName);
    
    @Query("SELECT csp FROM ConcreteShiftPlan csp LEFT JOIN FETCH csp.scheduledShifts WHERE csp.department.name = :departmentName")
    List<ConcreteShiftPlan> findByDepartmentNameWithShifts(@Param("departmentName") String departmentName);
}
