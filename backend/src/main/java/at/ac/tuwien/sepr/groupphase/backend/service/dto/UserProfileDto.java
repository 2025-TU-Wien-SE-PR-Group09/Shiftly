package at.ac.tuwien.sepr.groupphase.backend.service.dto;

public class UserProfileDto {

    private final String name;
    private final String email;
    private final String role;
    private final String department;

    public UserProfileDto(String name, String email, String role, String department) {
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
