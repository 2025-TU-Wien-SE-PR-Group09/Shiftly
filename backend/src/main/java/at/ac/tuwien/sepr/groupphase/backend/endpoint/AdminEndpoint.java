package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.AdminAuthCodeRestDto;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;

import static at.ac.tuwien.sepr.groupphase.backend.config.Constants.AUTH_CODE;

@RestController
@RequestMapping(value = "/api/v1/admin")
@ApiResponse(responseCode = "403", description = "Access denied")
@ApiResponse(responseCode = "404", description = "Given resource not found")
@ApiResponse(responseCode = "400", description = "Invalid request data")
@ApiResponse(responseCode = "409", description = "Conflict with existing data")
public class AdminEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @GetMapping(path = "/authCode", produces = MediaType.APPLICATION_JSON_VALUE)
    @RolesAllowed({"ADMIN"})
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "200", description = "Returns the admin authentication code")
    public AdminAuthCodeRestDto authCode() {
        LOGGER.trace("authCode()");
        return new AdminAuthCodeRestDto(AUTH_CODE);
    }
}
