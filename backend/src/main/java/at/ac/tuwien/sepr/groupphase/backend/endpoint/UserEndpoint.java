package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.ChangePasswordDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * @param dto the DTO containing old and new passwords
     * @return 200 OK if successful, or appropriate error otherwise
     */
    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordDto dto) {
        userService.changePasswordOfCurrentUser(dto);
        return ResponseEntity.ok().build();
    }

}
