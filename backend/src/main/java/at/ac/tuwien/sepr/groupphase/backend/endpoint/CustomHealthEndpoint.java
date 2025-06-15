package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;

/**
 * This endpoint is used for kubernetes health checks.
 */
@RestController
@RequestMapping("/health")
public class CustomHealthEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ApplicationContext applicationContext;
    private boolean status = true;

    @Autowired
    public CustomHealthEndpoint(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PermitAll
    @GetMapping
    @Operation(
        summary = "Health check endpoint",
        description = "This endpoint is used to check the health of the application. " +
            "It returns 'OK' if the application is healthy, otherwise it returns an internal server error."
    )
    public ResponseEntity<String> getHealth() {
        LOGGER.trace("getHealth()");

        if (status) {
            return ResponseEntity.ok("OK");
        }
        return ResponseEntity.internalServerError().build();
    }


    @PermitAll
    @GetMapping("/prepareShutdown")
    @Operation(
        summary = "Prepare for shutdown",
        description = "This endpoint is called before the pod is shut down to change the liveness state."
    )
    public void preShutdown() {
        LOGGER.trace("preShutdown()");

        AvailabilityChangeEvent.publish(applicationContext, LivenessState.BROKEN);
        status = false;
    }
}
