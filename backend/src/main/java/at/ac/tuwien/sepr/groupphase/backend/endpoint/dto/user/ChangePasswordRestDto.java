package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user;

import jakarta.validation.constraints.Pattern;


/**
 * REST DTO for password change requests.
 * Contains the old and new password entered by the user.
 */
public class ChangePasswordRestDto {


    /**
     * The new password to be set. Must be at least 8 characters long.
     */

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

    @Override
    public String toString() {
        return "ChangePasswordRestDto{" +
            "newPassword='" + newPassword + '\'' +
            '}';
    }
}
