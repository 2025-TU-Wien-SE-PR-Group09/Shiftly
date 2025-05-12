package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

public class LoginResponseDto {
    String jwt;

    public LoginResponseDto(String jwt) {
        this.jwt = jwt;
    }

    public String getJwt() {
        return jwt;
    }
}
