package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.MessageResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.RegisterRestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.RegisterTokenRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserInviteRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.UserDataDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.security.PermitAll;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import static at.ac.tuwien.sepr.groupphase.backend.config.Constants.AUTH_CODE;

@RestController
@RequestMapping(value = "/api/v1/registration")
@ApiResponse(responseCode = "403", description = "Access denied")
@ApiResponse(responseCode = "404", description = "Given resource not found")
@ApiResponse(responseCode = "400", description = "Invalid request data")
@ApiResponse(responseCode = "409", description = "Conflict with existing data")
public class RegistrationEndpoint {
    private final UserService userService;
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public RegistrationEndpoint(UserService userService) {
        this.userService = userService;
    }

    @PermitAll
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    @Operation(summary = "Register a user")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "201", description = "User registered successfully")
    public MessageResponseDto registerUser(@RequestBody @Valid RegisterRestDto registerRestDto) {
        LOGGER.trace("registerUser({})", registerRestDto);

        if (!registerRestDto.getCode().equals(AUTH_CODE)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token");
        }
        UserDataDto userDataDto = RegisterRestDto.from(registerRestDto);
        userService.createUser(userDataDto);
        return new MessageResponseDto("User registered successfully");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/invite", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Invite a new user",
        description = "Allows an admin to send an invitation email to a potential new user."
    )
    @ApiResponse(responseCode = "200", description = "Invitation sent successfully")
    @ApiResponse(responseCode = "400", description = "Email already in use")
    public MessageResponseDto inviteUser(@RequestBody @Valid UserInviteRequestDto request) {
        LOGGER.trace("inviteUser({})", request);

        userService.createInvitation(request.email());
        return new MessageResponseDto("Invitation sent to " + request.email());
    }

    @PostMapping(path = "/validate-token", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Validate an invitation token",
        description = "Validates if the provided token is still valid and returns the associated email."
    )
    @ApiResponse(responseCode = "200", description = "Token is valid")
    @ApiResponse(responseCode = "400", description = "Token is invalid, expired, or already used")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        LOGGER.trace("validateToken({})", token);

        String email = userService.validateInvitationToken(token);
        return ResponseEntity.ok(Map.of("email", email));
    }

    @PostMapping(path = "/register-with-token", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Register a new user with an invitation token",
        description = "Registers a new user using an invitation token."
    )
    @ApiResponse(responseCode = "201", description = "User registered successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data, token expired, or already used")
    @Transactional
    public ResponseEntity<?> registerWithToken(
        @RequestBody @Valid RegisterTokenRequestDto userData,
        @RequestParam(name = "arg1") String arg1) {
        //had to rename "token" parameter to "arg1" due to swagger generated api client in frontend.

        LOGGER.trace("registerWithToken({}, {})", userData, arg1);
        System.out.println("endpoint: " + userData + "\n token: " + arg1);
        userService.registerUserWithToken(userData, arg1);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}



