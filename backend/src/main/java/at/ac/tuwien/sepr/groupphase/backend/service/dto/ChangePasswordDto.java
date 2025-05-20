package at.ac.tuwien.sepr.groupphase.backend.service.dto;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ChangePasswordRestDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;


/**
 * Data Transfer Object for requesting a password change.
 *
 * <p>This object is used to transmit the old and new password of an
 * authenticated user who wants to change their password via the profile page.
 * </p>
 *
 * <p>Validation ensures both fields are provided and the new password
 * has a minimum length of 8 characters.
 * </p>
 */
public class ChangePasswordDto {


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


    public static ChangePasswordDto from(ChangePasswordRestDto restDto) {
        ChangePasswordDto dto = new ChangePasswordDto();
        dto.setNewPassword(restDto.getNewPassword());
        return dto;
    }
}
