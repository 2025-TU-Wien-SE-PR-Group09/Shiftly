package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.entity.ScheduledShift;
import at.ac.tuwien.sepr.groupphase.backend.entity.IcalSubscriptionToken;
import at.ac.tuwien.sepr.groupphase.backend.service.IcalSubscriptionTokenService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.ConcreteShiftPlanIcalDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.service.IcalService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.UserDepartmentDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.UserEmailDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.UserProfileDto;
import at.ac.tuwien.sepr.groupphase.backend.service.shift.ShiftPlanningService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.List;
import java.util.Date;
import java.util.Optional;

// iCal4j imports for test endpoint
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.RandomUidGenerator;
import net.fortuna.ical4j.model.DateTime;

@Tag(name = "iCal")
@RestController
@RequestMapping("/api/ical")
@ApiResponse(responseCode = "403", description = "Access denied")
@ApiResponse(responseCode = "404", description = "Given resource not found")
@ApiResponse(responseCode = "400", description = "Invalid request data")
@ApiResponse(responseCode = "409", description = "Conflict with existing data")
public class IcalEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final IcalService icalService;
    private final ShiftPlanningService shiftPlanningService;
    private final UserService userService;
    private final IcalSubscriptionTokenService tokenService;

    public IcalEndpoint(IcalService icalService, ShiftPlanningService shiftPlanningService,
                        UserService userService, IcalSubscriptionTokenService tokenService) {
        this.icalService = icalService;
        this.shiftPlanningService = shiftPlanningService;
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @RolesAllowed({"ADMIN", "SUPERVISOR", "EMPLOYEE"})
    @Operation(summary = "Download iCal for the user's shifts in a department's concrete shift plan")
    @ApiResponse(responseCode = "200", description = "iCal file successfully generated and returned")
    @ApiResponse(responseCode = "404", description = "Department or user's shift plan not found")
    @GetMapping(path = "/department/{departmentName}/shiftplan", produces = "text/calendar")
    @Transactional
    public ResponseEntity<String> downloadIcalForDepartmentShiftplan(
        @PathVariable("departmentName") String departmentName,
        Principal principal) {
        LOGGER.trace("downloadIcalForDepartmentShiftplan(departmentName={}, principal={})", departmentName, principal.getName());
        String userEmail = principal.getName();

        try {
            // Check if user has access to the department
            UserDepartmentDto userDepartment = userService.getUserByEmail(new UserEmailDto(userEmail));

            // Get current user's role to check if they're ADMIN
            UserProfileDto currentUser = userService.getCurrentUserProfile();
            boolean isAdmin = "ADMIN".equals(currentUser.getRole());

            // Admin has access to all departments, others only to their own
            boolean hasAccess = isAdmin || userDepartment.deparmentName().equals(departmentName);

            if (!hasAccess) {
                LOGGER.warn("User {} does not have access to department {}", userEmail, departmentName);
                throw new AccessDeniedException("User does not have access to this department");
            }

            // Get all shifts for the department
            List<ScheduledShift> shifts = shiftPlanningService.getAllPlans(departmentName).stream().flatMap(x -> x.getScheduledShifts().stream()).toList();

            // Generate iCal content
            String icalContent = icalService.generateIcal(new ConcreteShiftPlanIcalDto(shifts));

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + departmentName + "_shifts.ics\"");

            return ResponseEntity.ok()
                .headers(headers)
                .body(icalContent);
        } catch (NotFoundException e) {
            LOGGER.warn("Department or user's shift plan not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            LOGGER.warn("Access denied for user {} to department {}: {}", userEmail, departmentName, e.getMessage());
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            LOGGER.error("Error generating iCal for department {} and user {}: {}", departmentName, userEmail, e.getMessage());
            throw new RuntimeException("Error generating iCal", e);
        }
    }

    @RolesAllowed({"EMPLOYEE"})
    @Operation(summary = "Download iCal for the current employee's shifts")
    @ApiResponse(responseCode = "200", description = "iCal file successfully generated and returned")
    @ApiResponse(responseCode = "404", description = "Employee's shift plan not found")
    @GetMapping(path = "/employee/shifts", produces = "text/calendar")
    @Transactional
    public ResponseEntity<String> downloadEmployeeShiftsIcal(Principal principal) {
        LOGGER.trace("downloadEmployeeShiftsIcal(principal={})", principal.getName());
        String employeeEmail = principal.getName();

        try {
            // Get the employee's department
            String departmentName = userService.getDepartmentForUser(employeeEmail);
            LOGGER.info("Employee {} is in department: {}", employeeEmail, departmentName);

            if (departmentName == null) {
                LOGGER.warn("Employee {} is not assigned to any department", employeeEmail);
                return ResponseEntity.notFound().build();
            }



            List<ScheduledShift> shifts = shiftPlanningService.getAllPlans(departmentName).stream().flatMap(x -> x.getScheduledShifts().stream()).toList();

            LOGGER.info("Retrieved {} shifts from concrete plan", shifts.size());

            // Log details about each shift
            for (ScheduledShift shift : shifts) {
                LOGGER.info("Shift: {} ({} - {}) with {} assignments",
                    shift.getDescription(),
                    shift.getStart(),
                    shift.getEnd(),
                    shift.getAssignments() != null ? shift.getAssignments().size() : 0);

                if (shift.getAssignments() != null) {
                    for (var assignment : shift.getAssignments()) {
                        LOGGER.info("  Assignment: {} ({})",
                            assignment.getUser().getEmail(),
                            assignment.getUser().getFirstName() + " " + assignment.getUser().getLastName());
                    }
                }
            }

            // Generate iCal content with only the employee's shifts
            String icalContent = icalService.generateEmployeeIcal(new ConcreteShiftPlanIcalDto(shifts), employeeEmail);
            LOGGER.info("Generated iCal content with length: {}", icalContent.length());
            LOGGER.info("iCal content preview: {}", icalContent.substring(0, Math.min(500, icalContent.length())));

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"my_shifts.ics\"");

            return ResponseEntity.ok()
                .headers(headers)
                .body(icalContent);
        } catch (NotFoundException e) {
            LOGGER.warn("Employee's shift plan not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            LOGGER.error("Error generating iCal for employee {}: {}", employeeEmail, e.getMessage(), e);
            throw new RuntimeException("Error generating iCal", e);
        }
    }

    /*
    @RolesAllowed({"EMPLOYEE"})
    @Operation(summary = "Subscribe to employee's shifts calendar")
    @ApiResponse(responseCode = "200", description = "iCal file successfully generated and returned")
    @ApiResponse(responseCode = "404", description = "Employee's shift plan not found")
    @GetMapping(path = "/employee/shifts/subscribe", produces = "text/calendar")
    @Transactional
    public ResponseEntity<String> subscribeToEmployeeShiftsIcal(Principal principal) {
        LOGGER.trace("subscribeToEmployeeShiftsIcal(principal={})", principal.getName());
        String employeeEmail = principal.getName();

        try {
            // Get the employee's department
            String departmentName = userService.getDepartmentForUser(employeeEmail);
            if (departmentName == null) {
                LOGGER.warn("Employee {} is not assigned to any department", employeeEmail);
                return ResponseEntity.notFound().build();
            }

            // Get all shifts for the department
            List<ScheduledShift> shifts = shiftPlanningService.getCurrentConcretePlan(departmentName).getScheduledShifts();

            // Generate iCal content with only the employee's shifts
            String icalContent = icalService.generateEmployeeSubscriptionIcal(new ConcreteShiftPlanIcalDto(shifts), employeeEmail);

            HttpHeaders headers = new HttpHeaders();
            // Use attachment instead of inline for better compatibility with calendar applications
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"my_shifts.ics\"");
            // Add cache control headers to allow calendar applications to refresh the feed
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            headers.add(HttpHeaders.PRAGMA, "no-cache");
            headers.add(HttpHeaders.EXPIRES, "0");

            return ResponseEntity.ok()
                .headers(headers)
                .body(icalContent);
        } catch (NotFoundException e) {
            LOGGER.warn("Employee's shift plan not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            LOGGER.error("Error generating iCal for employee {}: {}", employeeEmail, e.getMessage());
            throw new RuntimeException("Error generating iCal", e);
        }
    }

     */

    @RolesAllowed({"EMPLOYEE"})
    @Operation(summary = "Generate personal subscription URL")
    @ApiResponse(responseCode = "200", description = "Personal subscription URL generated")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @GetMapping(path = "/employee/shifts/subscription-url", produces = "text/plain")
    public ResponseEntity<String> generateSubscriptionUrl(Principal principal) {
        LOGGER.trace("generateSubscriptionUrl(principal={})", principal.getName());
        String employeeEmail = principal.getName();

        try {
            // Get or create token using the service
            IcalSubscriptionToken token = tokenService.getOrCreateToken(employeeEmail);

            // Generate the subscription URL using the request's server URL
            String subscriptionUrl = String.format("%s/api/ical/employee/shifts/subscribe/%s",
                getServerUrl(), token.getToken());

            LOGGER.info("Generated subscription URL for employee {}: {}", employeeEmail, subscriptionUrl);

            return ResponseEntity.ok(subscriptionUrl);
        } catch (Exception e) {
            LOGGER.error("Error generating subscription URL for employee {}: {}", employeeEmail, e.getMessage());
            throw new RuntimeException("Error generating subscription URL", e);
        }
    }

    @RolesAllowed({"EMPLOYEE"})
    @Operation(summary = "Regenerate personal subscription URL with new token")
    @ApiResponse(responseCode = "200", description = "New personal subscription URL generated")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @GetMapping(path = "/employee/shifts/regenerate-subscription-url", produces = "text/plain")
    public ResponseEntity<String> regenerateSubscriptionUrl(Principal principal) {
        LOGGER.trace("regenerateSubscriptionUrl(principal={})", principal.getName());
        String employeeEmail = principal.getName();

        try {
            // Delete existing token and create a new one
            tokenService.deleteToken(employeeEmail);
            IcalSubscriptionToken newToken = tokenService.getOrCreateToken(employeeEmail);

            // Generate the subscription URL using the request's server URL
            String subscriptionUrl = String.format("%s/api/ical/employee/shifts/subscribe/%s",
                getServerUrl(), newToken.getToken());

            LOGGER.info("Regenerated subscription URL for employee {}: {}", employeeEmail, subscriptionUrl);

            return ResponseEntity.ok(subscriptionUrl);
        } catch (Exception e) {
            LOGGER.error("Error regenerating subscription URL for employee {}: {}", employeeEmail, e.getMessage());
            throw new RuntimeException("Error regenerating subscription URL", e);
        }
    }

    @Operation(summary = "Subscribe to employee's shifts calendar using token")
    @ApiResponse(responseCode = "200", description = "iCal file successfully generated and returned")
    @ApiResponse(responseCode = "404", description = "Employee's shift plan not found")
    @ApiResponse(responseCode = "403", description = "Invalid or expired token")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @GetMapping(path = "/employee/shifts/subscribe/{token}", produces = "text/calendar")
    @Transactional
    public ResponseEntity<String> subscribeToEmployeeShiftsIcalWithToken(@PathVariable("token") String token) {
        LOGGER.info("subscribeToEmployeeShiftsIcalWithToken(token={})", token);

        // Validate and update the token using the service
        Optional<IcalSubscriptionToken> tokenOpt = tokenService.validateAndUpdateToken(token);

        if (tokenOpt.isEmpty()) {
            LOGGER.warn("Invalid subscription token: {}", token);
            return ResponseEntity.status(403).body("Invalid or expired subscription token");
        }

        IcalSubscriptionToken subscriptionToken = tokenOpt.get();
        String employeeEmail = subscriptionToken.getUserEmail();
        LOGGER.info("Found employee email {} for token {}", employeeEmail, token);

        try {
            // Get the employee's department
            String departmentName = userService.getDepartmentForUser(employeeEmail);
            LOGGER.info("Employee {} is in department: {}", employeeEmail, departmentName);

            if (departmentName == null) {
                LOGGER.warn("Employee {} is not assigned to any department", employeeEmail);
                return ResponseEntity.notFound().build();
            }



            List<ScheduledShift> shifts = shiftPlanningService.getAllPlans(departmentName).stream().flatMap(x -> x.getScheduledShifts().stream()).toList();

            LOGGER.info("Retrieved {} shifts from concrete plan", shifts.size());

            // Generate iCal content with only the employee's shifts
            String icalContent = icalService.generateEmployeeSubscriptionIcal(new ConcreteShiftPlanIcalDto(shifts), employeeEmail);
            LOGGER.info("Generated iCal content with length: {}", icalContent.length());

            HttpHeaders headers = new HttpHeaders();
            // Use attachment instead of inline for better compatibility with calendar applications
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"my_shifts.ics\"");
            // Add cache control headers to allow calendar applications to refresh the feed
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            headers.add(HttpHeaders.PRAGMA, "no-cache");
            headers.add(HttpHeaders.EXPIRES, "0");

            LOGGER.info("Generated subscription iCal for employee {} with token {}", employeeEmail, token);

            return ResponseEntity.ok()
                .headers(headers)
                .body(icalContent);
        } catch (NotFoundException e) {
            LOGGER.warn("Employee's shift plan not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            LOGGER.error("Error generating iCal for employee {} with token {}: {}", employeeEmail, token, e.getMessage(), e);
            throw new RuntimeException("Error generating iCal", e);
        }
    }

    @Operation(summary = "Test iCal endpoint without authentication")
    @ApiResponse(responseCode = "200", description = "Test iCal file generated")
    @GetMapping(path = "/test", produces = "text/calendar")
    public ResponseEntity<String> testIcal() {
        LOGGER.info("Test iCal endpoint called");

        Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId("-//Shyft//iCal4j 3.2.10//EN"));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);

        // Add a test event
        Date now = new Date();
        Date oneHourLater = new Date(now.getTime() + 3600000); // 1 hour later

        VEvent event = new VEvent(new DateTime(now), new DateTime(oneHourLater), "Test Event");
        Uid uid = new RandomUidGenerator().generateUid();
        event.getProperties().add(uid);
        event.getProperties().add(new Description("This is a test event"));

        calendar.getComponents().add(event);

        String icalContent = calendar.toString();
        LOGGER.info("Test iCal content length: {}", icalContent.length());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"test.ics\"");

        return ResponseEntity.ok()
            .headers(headers)
            .body(icalContent);
    }

    /**
     * Helper method to get the server URL from the current request.
     * This ensures the subscription URL uses the correct server address.
     */
    private String getServerUrl() {
        // Since user is using port forwarding, localhost is fine
        return "http://localhost:8080";
    }
}