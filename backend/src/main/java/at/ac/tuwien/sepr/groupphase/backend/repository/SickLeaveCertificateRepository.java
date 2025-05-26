package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.SickLeaveCertificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SickLeaveCertificateRepository extends JpaRepository<SickLeaveCertificate, Long> {
    List<SickLeaveCertificate> findByEmployee_Email(String email);
}
