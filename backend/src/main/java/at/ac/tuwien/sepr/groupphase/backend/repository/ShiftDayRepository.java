package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftDay;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShiftDayRepository extends JpaRepository<ShiftDay, Long> {
}