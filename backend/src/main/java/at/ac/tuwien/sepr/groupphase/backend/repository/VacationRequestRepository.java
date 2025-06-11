package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.VacationRequest;

import at.ac.tuwien.sepr.groupphase.backend.type.VacationStatus;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VacationRequestRepository extends JpaRepository<VacationRequest, Long> {
    List<VacationRequest> findByEmployee(ApplicationUser user);

    List<VacationRequest> findByStatusAndEmployeeDepartmentName(VacationStatus status, String departmentName);


    List<VacationRequest> findByEmployeeAndStatus(ApplicationUser employee, VacationStatus status);
}