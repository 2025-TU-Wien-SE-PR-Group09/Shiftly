package at.ac.tuwien.sepr.groupphase.backend.service.dto.user;

public class UserProfileDto {


    private String firstName;
    private String lastName;
    private final String email;
    private final String role;
    private final String department;

    public UserProfileDto(String firstName, String lastName, String email, String role, String department) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.department = department;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
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
