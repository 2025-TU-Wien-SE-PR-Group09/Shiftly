package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.ChangePasswordRestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserProfileRestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.ChangePasswordDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.UserProfileDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;


/**
 * REST controller for handling user-related actions such as password change.
 */
@RestController
@RequestMapping("/api/users")
@ApiResponse(responseCode = "403", description = "Access denied")
@ApiResponse(responseCode = "404", description = "Given resource not found")
@ApiResponse(responseCode = "400", description = "Invalid request data")
@ApiResponse(responseCode = "409", description = "Conflict with existing data")
public class UserEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UserService userService;

    public UserEndpoint(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("isAuthenticated() and !hasRole('ADMIN')")
    @PutMapping(path = "/me/password", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Change password of the currently authenticated user",
        description = "Allows a non-admin user to change their password. The user must be authenticated."
    )
    @ApiResponse(responseCode = "204", description = "Password changed successfully")
    @Transactional
    public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordRestDto restDto) {
        LOGGER.trace("changePassword({})", restDto);

        ChangePasswordDto dto = ChangePasswordDto.from(restDto);
        userService.changePasswordOfCurrentUser(dto);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get current user profile",
        description = "Retrieves the profile information of the currently authenticated user, including name, email, role, and department."
    )
    @ApiResponse(responseCode = "200", description = "Returns the current user's profile information")
    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserProfileRestDto getCurrentUserProfile() {
        LOGGER.trace("getCurrentUserProfile()");

        UserProfileDto serviceDto = userService.getCurrentUserProfile();

        return new UserProfileRestDto(
            serviceDto.getFirstName(),
            serviceDto.getLastName(),
            serviceDto.getEmail(),
            serviceDto.getRole(),
            serviceDto.getDepartment()
        );
    }

}