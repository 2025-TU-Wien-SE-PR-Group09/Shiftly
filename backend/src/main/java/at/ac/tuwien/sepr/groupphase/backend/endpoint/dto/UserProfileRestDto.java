package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * REST DTO for returning user profile data.
 * Contains name, email, role, and department information.
 */
public class UserProfileRestDto {

  @Size(min=4 ,max = 100)
  private String name;

  @NotNull(message = "Email must not be null")
  @Email(message = "Email must be a valid email address")
  private String email;


  private String role;
  private String department;


  public UserProfileRestDto(String name, String email, String role, String department) {
    this.name = name;
    this.email = email;
    this.role = role;
    this.department = department;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public String getRole() {
    return role;
  }

  public String getDepartment() {
    return department;
  }


}
