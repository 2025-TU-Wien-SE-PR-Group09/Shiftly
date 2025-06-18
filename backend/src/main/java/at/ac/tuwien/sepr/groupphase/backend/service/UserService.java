package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.ApplicationUserResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.RegisterRestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.RegisterTokenRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.TokenAlreadyUsedException;
import at.ac.tuwien.sepr.groupphase.backend.exception.TokenExpiredException;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.department.DepartmentNameDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.ChangePasswordDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.UserDataDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.UserDepartmentDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.UserEmailDto;
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

    /**
     * Retrieves a user by their email address.
     *
     * @param emailDto dto containing the email address of the user to retrieve
     * @return a {@link UserProfileDto} containing the user's profile information
     * @throws NotFoundException if no user with the given email exists
     */
    UserDepartmentDto getUserByEmail(UserEmailDto emailDto) throws NotFoundException;

    /**
     * Checks if the currently authenticated user has access to the department of the given user.
     *
     * @param departmentNameDto the user department dto containing the user's email and department name
     * @throws AccessDeniedException if the currently authenticated user does not have access to the department
     */
    void checkAccessToDepartment(DepartmentNameDto departmentNameDto) throws AccessDeniedException;


    /**
     * Creates an invitation for a new user and sends an email with the invitation link.
     *
     * @param email Email address of the user to be invited
     * @throws IllegalArgumentException if the email address is already in use
     */
    void createInvitation(String email) throws IllegalArgumentException;

    /**
     * Validates if an invitation token is valid.
     *
     * @param token The token to validate
     * @return The email address associated with this token
     * @throws NotFoundException         if the token does not exist
     * @throws TokenExpiredException     if the token has expired
     * @throws TokenAlreadyUsedException if the token was already used
     */
    String validateInvitationToken(String token) throws NotFoundException, TokenExpiredException, TokenAlreadyUsedException;

    /**
     * Registers a new user with an invitation token.
     *
     * @param userData User data for registration
     * @param token    The invitation token
     * @throws NotFoundException         if the token does not exist
     * @throws TokenExpiredException     if the token has expired
     * @throws TokenAlreadyUsedException if the token was already used
     * @throws IllegalArgumentException  if the email address does not match the token
     */
    void registerUserWithToken(RegisterTokenRequestDto userData, String token) throws NotFoundException, TokenExpiredException, TokenAlreadyUsedException, IllegalArgumentException;
}