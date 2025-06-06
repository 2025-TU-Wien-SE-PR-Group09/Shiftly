package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.ChangePasswordRestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserProfileRestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.ChangePasswordDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.UserProfileDto;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * REST controller for handling user-related actions such as password change.
 */
@RestController
@RequestMapping("/api/users")
public class UserEndpoint {

    private final UserService userService;

    /**
     * Constructor for injecting the user service.
     *
     * @param userService the user service to be injected
     */
    public UserEndpoint(UserService userService) {
        this.userService = userService;
    }

    /**
     * Changes the password of the currently authenticated non-admin user.
     *
     * <p>Only authenticated users without the {@code ADMIN} role are allowed to access this endpoint.
     *
     * <p>If the request is unauthenticated, or the user has the {@code ADMIN} role,
     * access is denied with a 403 Forbidden response.
     *
     * @param restDto DTO containing the new password (validated)
     * @return 200 OK on successful password change,
     *     400 Bad Request if the input is invalid,
     *     403 Forbidden if the user is not authenticated or has the ADMIN role
     */
    @PreAuthorize("isAuthenticated() and !hasRole('ADMIN')")
    @PutMapping(path = "/me/password", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordRestDto restDto) {
        ChangePasswordDto dto = ChangePasswordDto.from(restDto);
        userService.changePasswordOfCurrentUser(dto);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieve the profile information of the currently authenticated user.
     *
     * @return a {@link UserProfileRestDto} containing user details such as name,
     *     email, role, and department
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserProfileRestDto getCurrentUserProfile() {
        UserProfileDto serviceDto = userService.getCurrentUserProfile();

        return new UserProfileRestDto(
            serviceDto.getName(),
            serviceDto.getEmail(),
            serviceDto.getRole(),
            serviceDto.getDepartment()
        );
    }

}
