package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.ScheduledShift;
import at.ac.tuwien.sepr.groupphase.backend.entity.ScheduledShiftId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduledShiftRepository extends JpaRepository<ScheduledShift, ScheduledShiftId> {
    List<ScheduledShift> findByDepartmentId(Long departmentId);
}
