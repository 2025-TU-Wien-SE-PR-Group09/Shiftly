package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.AdminAuthCodeRestDto;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static at.ac.tuwien.sepr.groupphase.backend.config.Constants.AUTH_CODE;

@RestController
@RequestMapping(value = "/api/v1/admin")
public class AdminEndpoint {

    @GetMapping(path = "/authCode", produces = MediaType.APPLICATION_JSON_VALUE)
    @RolesAllowed({"ADMIN"})
    @ResponseStatus(HttpStatus.OK)
    public AdminAuthCodeRestDto authCode() { // todo use a dto instead
        return new AdminAuthCodeRestDto(AUTH_CODE);
    }
}
