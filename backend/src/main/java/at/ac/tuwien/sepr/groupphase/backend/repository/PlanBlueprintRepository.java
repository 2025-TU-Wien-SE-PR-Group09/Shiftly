package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Department;
import at.ac.tuwien.sepr.groupphase.backend.entity.PlanBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.PlanId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanBlueprintRepository extends JpaRepository<PlanBlueprint, PlanId> {

    Optional<PlanBlueprint> findByDepartment(Department department);

}