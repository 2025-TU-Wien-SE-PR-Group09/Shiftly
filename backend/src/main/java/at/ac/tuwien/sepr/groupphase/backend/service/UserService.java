package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.LoginResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserDataDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserRoleDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import org.aspectj.weaver.ast.Not;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserService extends UserDetailsService {

    /**
     * Find a user in the context of Spring Security based on the email address.
     * <br>
     * For more information have a look at this tutorial:
     * <a href="https://www.baeldung.com/spring-security-authentication-with-a-database">Spring Security Tutorial</a>
     *
     * @param email the email address
     * @return a Spring Security user
     * @throws UsernameNotFoundException is thrown if the specified user does not exists
     */
    @Override
    UserDetails loadUserByUsername(String email) throws UsernameNotFoundException;

    /**
     * Log in a user.
     *
     * @param userLoginDto login credentials
     * @return {@link LoginResponseDto} containing the JWT, if successful
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are bad
     */
    LoginResponseDto login(UserDataDto userLoginDto);

    /**
     * Create a user with a given email and password. If the user already exists, the password will be updated.
     *
     * @param userData {@link UserDataDto} object containing the username and password
     */
    ApplicationUser createOrChangePassword(UserDataDto userData);

    /**
     * Assign a role to a user. If the role does not exist, it is created by this method.
     *
     * @param userRole {@link UserRoleDto} object containing the email and role of the user
     * @throws NotFoundException when the user does not exist
     */
    void assignRoleToUser(UserRoleDto userRole) throws NotFoundException;
}
