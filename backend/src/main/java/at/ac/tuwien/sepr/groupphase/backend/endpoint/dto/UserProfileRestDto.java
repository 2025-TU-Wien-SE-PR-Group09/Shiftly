package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

/**
 * REST DTO for returning user profile data.
 * Contains name, email, role, and department information.
 */
public class UserProfileRestDto {

    private String name;
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
