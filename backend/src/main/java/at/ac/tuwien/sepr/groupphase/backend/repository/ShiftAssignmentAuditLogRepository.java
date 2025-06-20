package at.ac.tuwien.sepr.groupphase.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftAssignmentAuditLog;

@Repository
public interface ShiftAssignmentAuditLogRepository extends JpaRepository<ShiftAssignmentAuditLog, Long> {

}