package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.VacationRequest;

import at.ac.tuwien.sepr.groupphase.backend.type.VacationStatus;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VacationRequestRepository extends JpaRepository<VacationRequest, Long> {
    /**
     * Finds all vacation requests for a specific employee.
     *
     * @param user the employee whose vacation requests are to be found
     * @return a list of vacation requests for the specified employee
     */
    List<VacationRequest> findByEmployee(ApplicationUser user);

    /**
     * Finds all vacation requests for a specific departmentName and status.
     *
     * @param departmentName the department name
     * @param status the status of the vacation requests to be found
     * @return a list of vacation requests matching the criteria
     */
    List<VacationRequest> findByStatusAndEmployeeDepartmentName(VacationStatus status, String departmentName);

    /**
     * Finds all vacation requests for a specific employee with a given status.
     *
     * @param employeeEmail the employee email whose vacation requests are to be found
     * @param status the status of the vacation requests to be found
     * @return a list of vacation requests matching the criteria
     */
    List<VacationRequest> findByEmployeeEmailAndStatus(String employeeEmail, VacationStatus status);
}