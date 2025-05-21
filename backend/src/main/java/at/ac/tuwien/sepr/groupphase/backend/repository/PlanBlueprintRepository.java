package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.PlanBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.PlanId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanBlueprintRepository extends JpaRepository<PlanBlueprint, PlanId> {
}