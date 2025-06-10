package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.ScheduledShift;
import at.ac.tuwien.sepr.groupphase.backend.entity.ScheduledShiftId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduledShiftRepository extends JpaRepository<ScheduledShift, ScheduledShiftId> {
    @Query("""
        SELECT s FROM ScheduledShift s
        WHERE s.start >= :start
        AND s.start <= :end
        """)
    List<ScheduledShift> findByStartBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<ScheduledShift> findByDepartmentName(String departmentName);
}
