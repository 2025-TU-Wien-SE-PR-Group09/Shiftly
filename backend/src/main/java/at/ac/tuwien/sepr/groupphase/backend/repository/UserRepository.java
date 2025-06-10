package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<ApplicationUser, String> {
    Optional<ApplicationUser> findByEmail(String email);

    @Query("SELECT u FROM ApplicationUser u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<ApplicationUser> findByEmailWithRoles(@Param("email") String email);

    @Query("SELECT u FROM ApplicationUser u JOIN u.roles r WHERE r.name = :roleName")
    List<ApplicationUser> findAllByRoleName(@Param("roleName") String roleName);

    @Query("SELECT u FROM ApplicationUser u WHERE u.department.id = :departmentId")
    List<ApplicationUser> findAllByDepartmentId(@Param("departmentId") Long departmentId);

    @Query("SELECT u FROM ApplicationUser u LEFT JOIN u.roles r WHERE r IS NULL")
    List<ApplicationUser> findAllWithNoRole();

    @Query("SELECT u FROM ApplicationUser u WHERE u.department.id = :departmentId")
    List<ApplicationUser> findByDepartmentId(@Param("departmentId") Long departmentId);
}
