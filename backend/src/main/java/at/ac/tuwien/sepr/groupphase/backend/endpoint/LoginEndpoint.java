package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.LoginResponseRestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserDataLoginDto;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.security.PermitAll;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;

@RestController
@RequestMapping(value = "/api/v1/authentication")
@ApiResponse(responseCode = "403", description = "Access denied")
@ApiResponse(responseCode = "404", description = "Given resource not found")
@ApiResponse(responseCode = "400", description = "Invalid request data")
@ApiResponse(responseCode = "409", description = "Conflict with existing data")
public class LoginEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final AuthService authService;

    public LoginEndpoint(AuthService authService) {
        this.authService = authService;
    }

    @PermitAll
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    @Operation(summary = "Login a user")
    @ApiResponse(responseCode = "200", description = "Returns a JWT token and user information upon successful login")
    public LoginResponseRestDto login(@Valid @RequestBody UserDataLoginDto userLoginRestDto) {
        LOGGER.trace("login({})", userLoginRestDto);

        return LoginResponseRestDto.from(authService.login(userLoginRestDto));
    }
}
