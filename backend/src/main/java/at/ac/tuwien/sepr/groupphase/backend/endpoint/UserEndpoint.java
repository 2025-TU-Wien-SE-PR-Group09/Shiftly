package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ChangePasswordRestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserProfileRestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.ChangePasswordDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.UserProfileDto;
import jakarta.validation.Valid;
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
     * Change the password of the currently authenticated user.
     *
     * @param restDto the DTO containing old and new passwords
     * @return 200 OK if successful, or appropriate error otherwise
     */
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me/password")
    @Transactional
    public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordRestDto restDto) {
        ChangePasswordDto dto = ChangePasswordDto.from(restDto);
        userService.changePasswordOfCurrentUser(dto);
        return ResponseEntity.ok().build();
    }

    /**
     * Get the profile information of the currently authenticated user.
     *
     * @return a UserProfileRestDto with user details
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<UserProfileRestDto> getCurrentUserProfile() {
        UserProfileDto serviceDto = userService.getCurrentUserProfile();

        UserProfileRestDto restDto = new UserProfileRestDto(
            serviceDto.getName(),
            serviceDto.getEmail(),
            serviceDto.getRole(),
            serviceDto.getDepartment()
        );

        return ResponseEntity.ok(restDto);
    }

}
