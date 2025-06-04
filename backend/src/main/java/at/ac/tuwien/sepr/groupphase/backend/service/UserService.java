package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ApplicationUserResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.ChangePasswordDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.UserDataDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.UserEmailDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.UserProfileDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.UserRoleDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;

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
     */
    void changePasswordOfCurrentUser(ChangePasswordDto dto);

    /**
     * Retrieves the profile information of the currently authenticated user.
     *
     * @return a {@link UserProfileDto} containing user name, email, role, and department
     */
    UserProfileDto getCurrentUserProfile();


    /**
     * Create a new user with a given email and password. The email must not be used by any other user yet.
     *
     * @param userData {@link UserDataDto} object containing the username and password
     */
    void createUser(UserDataDto userData);

    /**
     * Retrieves all users who are supervisors.
     *
     * @return a {@link ApplicationUser} list containing all supervisors
     */
    List<ApplicationUserResponseDto> getAllSupervisors();

    /**
     * Retrieves all users who are available, also who do not have a role.
     *
     * @return a {@link List<ApplicationUser>} containing all available users
     */
    List<ApplicationUserResponseDto> getAllAvailableUsers();
}
