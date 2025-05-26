package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.ConcreteShiftPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConcreteShiftPlanRepository extends JpaRepository<ConcreteShiftPlan, Long> {
    List<ConcreteShiftPlan> findByDepartmentId(Long departmentId);
}
