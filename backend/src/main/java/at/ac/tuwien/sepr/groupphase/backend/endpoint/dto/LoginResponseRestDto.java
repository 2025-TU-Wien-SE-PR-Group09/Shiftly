package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.service.dto.LoginResponseDto;

public class LoginResponseRestDto {
    String jwt;

    public LoginResponseRestDto() {

    }

    public LoginResponseRestDto(String jwt) {
        this.jwt = jwt;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    /**
     * Converts a {@link LoginResponseDto} to a {@link LoginResponseRestDto}.
     *
     * @param loginDto the {@link LoginResponseDto} to convert
     * @return the converted {@link LoginResponseRestDto}
     */
    public static LoginResponseRestDto from(LoginResponseDto loginDto) {
        return new LoginResponseRestDto(loginDto.getJwt());
    }
}
