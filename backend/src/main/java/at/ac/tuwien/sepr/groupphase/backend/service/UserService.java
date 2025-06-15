package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.ApplicationUserResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.ChangePasswordDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.UserDataDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.UserProfileDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.UserRoleDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

public interface UserService {
    /**
     * Create a user with a given email and password. If the user already exists, the password will be updated.
     * Will mostly be used to change/add the admin user.
     *
     * @param userData {@link UserDataDto} object containing the username and password
     */
    void createOrChangePassword(UserDataDto userData);

    /**
     * Assign a role to a user. If the role does not exist, it is created by this method.
     *
     * @param userRole {@link UserRoleDto} object containing the email and role of the user
     * @throws NotFoundException when the user does not exist
     */
    void assignRoleToUser(UserRoleDto userRole) throws NotFoundException;

    /**
     * Change the password of the currently authenticated user.
     *
     * @param dto contains the old and new password
     * @throws NotFoundException if the user does not exist
     * @throws AccessDeniedException if the user is not allowed to change the password
     */
    void changePasswordOfCurrentUser(ChangePasswordDto dto) throws NotFoundException, AccessDeniedException;

    /**
     * Retrieves the profile information of the currently authenticated user.
     *
     * @return a {@link UserProfileDto} containing user name, email, role, and department
     * @throws NotFoundException if the user is not found
     */
    UserProfileDto getCurrentUserProfile() throws NotFoundException;


    /**
     * Create a new user with a given email and password. The email must not be used by any other user yet.
     *
     * @param userData {@link UserDataDto} object containing the username and password
     * @throws IllegalArgumentException if an user with the given email already exists
     */
    void createUser(UserDataDto userData) throws IllegalArgumentException;

    /**
     * Retrieves all users who are supervisors.
     *
     * @param hasDepartment if true, only supervisors with a department are returned
     * @return a {@link ApplicationUser} list containing all supervisors
     */
    List<ApplicationUserResponseDto> getAllSupervisors(boolean hasDepartment);

    /**
     * Retrieves all users who are available, also who do not have a role.
     *
     * @return a {@link List} containing all {@link ApplicationUser} objects that describe available users
     */
    List<ApplicationUserResponseDto> getAllAvailableUsers();
}
