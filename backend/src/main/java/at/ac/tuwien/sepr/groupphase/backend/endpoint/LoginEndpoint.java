package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.LoginResponseRestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserDataRestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.UserDataDto;
import jakarta.annotation.security.PermitAll;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
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
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public LoginResponseRestDto login(@Valid @RequestBody UserDataRestDto userLoginRestDto) {
        UserDataDto userLoginDto = UserDataDto.from(userLoginRestDto);
        return LoginResponseRestDto.from(authService.login(userLoginDto));
    }
}
