package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.service.dto.ChangePasswordDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.UserDataDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.UserRoleDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;

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
     * Create a new user with a given email and password. The email must not be used by any other user yet.
     *
     * @param userData {@link UserDataDto} object containing the username and password
     */
    void createUser(UserDataDto userData);

}
