package at.ac.tuwien.sepr.groupphase.backend.service;


import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserDataLoginDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.LoginResponseDto;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface AuthService extends UserDetailsService {
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
    LoginResponseDto login(UserDataLoginDto userLoginDto) throws BadCredentialsException;

    /**
     * Retrieve the currently authenticated user from the security context.
     *
     * @return the authenticated {@link ApplicationUser}
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException if the user cannot be found
     */
    ApplicationUser getCurrentUser() throws UsernameNotFoundException;

}
