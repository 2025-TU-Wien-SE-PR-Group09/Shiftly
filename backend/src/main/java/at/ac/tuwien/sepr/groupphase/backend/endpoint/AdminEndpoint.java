package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.LoginResponseRestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserDataRestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.UserDataDto;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.Principal;

@RestController
@RequestMapping(value = "/api/v1/admin")
public class AdminEndpoint {

    @GetMapping
    @RolesAllowed({"ADMIN", ""})
    public String test(Principal principal) {
        return "ADMIN ONLY";
    }
}
