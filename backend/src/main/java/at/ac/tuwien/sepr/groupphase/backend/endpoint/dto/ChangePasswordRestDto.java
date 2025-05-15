package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * REST DTO for password change requests.
 * Contains the old and new password entered by the user.
 */
public class ChangePasswordRestDto {



    /**
     * The new password to be set. Must be at least 8 characters long.
     */
    @NotBlank(message = "New password must not be blank")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
        message = "New password must be at least 8 characters long and include uppercase, lowercase and a digit"
    )
    private String newPassword;


    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
