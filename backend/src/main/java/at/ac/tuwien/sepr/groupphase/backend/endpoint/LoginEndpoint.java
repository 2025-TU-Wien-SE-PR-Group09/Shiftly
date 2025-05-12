package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.LoginResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserDataDto;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import jakarta.annotation.security.PermitAll;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/authentication")
public class LoginEndpoint {

    private final AuthService authService;

    public LoginEndpoint(AuthService authService) {
        this.authService = authService;
    }

    @PermitAll
    @PostMapping
    public LoginResponseDto login(@RequestBody UserDataDto userLoginDto) {
        return authService.login(userLoginDto);
    }
}
