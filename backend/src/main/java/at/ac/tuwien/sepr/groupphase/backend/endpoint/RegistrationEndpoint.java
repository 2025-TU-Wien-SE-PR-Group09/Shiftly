package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.MessageResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RegisterRestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.UserDataDto;
import jakarta.annotation.security.PermitAll;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import static at.ac.tuwien.sepr.groupphase.backend.config.Constants.AUTH_CODE;

@RestController
@RequestMapping(value = "/api/v1/registration")
public class RegistrationEndpoint {
    private final UserService userService;

    public RegistrationEndpoint(UserService userService) {
        this.userService = userService;
    }

    @PermitAll
    @PostMapping
    @Transactional
    public MessageResponseDto registerUser(@RequestBody RegisterRestDto registerRestDto) {
        if (!registerRestDto.getCode().equals(AUTH_CODE)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Ungültiger oder fehlender Token");
        }
        UserDataDto userDataDto = RegisterRestDto.from(registerRestDto);
        userService.createOrChangePassword(userDataDto);
        return new MessageResponseDto("User registered successfully");
    }
}

