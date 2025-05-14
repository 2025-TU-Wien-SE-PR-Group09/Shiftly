package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserDataRestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.UserDataDto;
import jakarta.transaction.Transactional;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/registration")
public class RegistrationEndpoint {
    private final UserService userService;
    public RegistrationEndpoint(UserService userService) { this.userService = userService; }

    //todo review
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public UserDataRestDto registerUser(@RequestBody UserDataRestDto userDataRestDto) {
        UserDataDto userDataDto = UserDataDto.from(userDataRestDto);
        userService.createOrChangePassword(userDataDto);
        return userDataRestDto;
    }
}

