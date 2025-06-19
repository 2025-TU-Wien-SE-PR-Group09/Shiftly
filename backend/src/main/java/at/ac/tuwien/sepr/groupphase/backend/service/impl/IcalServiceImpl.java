package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.ScheduledShift;
import at.ac.tuwien.sepr.groupphase.backend.entity.ScheduledShiftAssignment;
import at.ac.tuwien.sepr.groupphase.backend.service.IcalService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.ConcreteShiftPlanIcalDto;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.model.property.XProperty;
import net.fortuna.ical4j.util.RandomUidGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IcalServiceImpl implements IcalService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final RandomUidGenerator uidGenerator = new RandomUidGenerator();

    @Override
    public String generateIcal(ConcreteShiftPlanIcalDto shiftPlanDto) {
        LOGGER.debug("Generating iCal for {} shifts", shiftPlanDto.scheduledShifts().size());

        Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId("-//Shyft//iCal4j 3.2.10//EN"));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);

        int eventCount = 0;
        for (ScheduledShift shift : shiftPlanDto.scheduledShifts()) {
            LOGGER.debug("Processing shift: {} with {} assignments", shift.getDescription(),
                shift.getAssignments() != null ? shift.getAssignments().size() : 0);

            // Convert LocalDateTime to Date
            Date startDate = Date.from(shift.getStart().atZone(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(shift.getEnd().atZone(ZoneId.systemDefault()).toInstant());

            // Create event with shift description as title
            VEvent event = new VEvent(new DateTime(startDate), new DateTime(endDate), shift.getDescription());

            // Add unique ID
            Uid uid = uidGenerator.generateUid();
            event.getProperties().add(uid);

            // Build description with department name and assigned employees
            String departmentName = shift.getDepartment() != null ? shift.getDepartment().getName() : "Unknown Department";
            StringBuilder descBuilder = new StringBuilder();
            descBuilder.append("Department: ").append(departmentName);

            // Add assigned employees to description
            if (shift.getAssignments() != null && !shift.getAssignments().isEmpty()) {
                List<String> assignedUsers = shift.getAssignments().stream()
                    .map(assignment -> {
                        String firstName = assignment.getUser().getFirstName();
                        String lastName = assignment.getUser().getLastName();
                        String email = assignment.getUser().getEmail();
                        return firstName + " " + lastName + " (" + email + ")";
                    })
                    .collect(Collectors.toList());

                descBuilder.append("\nAssigned employees: ").append(String.join(", ", assignedUsers));
                LOGGER.debug("Shift '{}' has {} assigned employees: {}", shift.getDescription(), assignedUsers.size(), assignedUsers);
            } else {
                descBuilder.append("\nNo employees assigned");
                LOGGER.debug("Shift '{}' has no assigned employees", shift.getDescription());
            }

            event.getProperties().add(new Description(descBuilder.toString()));

            // Add event to calendar
            calendar.getComponents().add(event);
            eventCount++;
        }

        LOGGER.debug("Generated {} events for {} shifts", eventCount, shiftPlanDto.scheduledShifts().size());
        return calendar.toString();
    }

    @Override
    public String generateEmployeeIcal(ConcreteShiftPlanIcalDto shiftPlanDto, String employeeEmail) {
        LOGGER.info("Generating employee-specific iCal for email {} from {} shifts", employeeEmail, shiftPlanDto.scheduledShifts().size());

        // Filter shifts to only include those assigned to the specified employee
        List<ScheduledShift> employeeShifts = shiftPlanDto.scheduledShifts().stream()
            .filter(shift -> {
                boolean hasAssignments = shift.getAssignments() != null && !shift.getAssignments().isEmpty();
                boolean isAssigned = hasAssignments && shift.getAssignments().stream()
                    .anyMatch(assignment -> assignment.getUser().getEmail().equals(employeeEmail));

                LOGGER.info("Shift '{}': hasAssignments={}, isAssigned={}",
                    shift.getDescription(), hasAssignments, isAssigned);

                return isAssigned;
            })
            .collect(Collectors.toList());

        LOGGER.info("Found {} shifts assigned to employee {}", employeeShifts.size(), employeeEmail);

        if (employeeShifts.isEmpty()) {
            LOGGER.warn("No shifts found for employee {}", employeeEmail);
        }

        Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId("-//Shyft//iCal4j 3.2.10//EN"));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);

        int eventCount = 0;
        for (ScheduledShift shift : employeeShifts) {
            LOGGER.info("Processing employee shift: {} with {} assignments", shift.getDescription(),
                shift.getAssignments() != null ? shift.getAssignments().size() : 0);

            // Convert LocalDateTime to Date
            Date startDate = Date.from(shift.getStart().atZone(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(shift.getEnd().atZone(ZoneId.systemDefault()).toInstant());

            LOGGER.info("Shift '{}': start={}, end={}", shift.getDescription(), startDate, endDate);

            // Create event with shift description as title
            VEvent event = new VEvent(new DateTime(startDate), new DateTime(endDate), shift.getDescription());

            // Add unique ID
            Uid uid = uidGenerator.generateUid();
            event.getProperties().add(uid);

            // Build description with department name and all assigned employees
            String departmentName = shift.getDepartment() != null ? shift.getDepartment().getName() : "Unknown Department";
            StringBuilder descBuilder = new StringBuilder();
            descBuilder.append("Department: ").append(departmentName);

            // Add all assigned employees to description (not just the current employee)
            if (shift.getAssignments() != null && !shift.getAssignments().isEmpty()) {
                List<String> assignedUsers = shift.getAssignments().stream()
                    .map(assignment -> {
                        String firstName = assignment.getUser().getFirstName();
                        String lastName = assignment.getUser().getLastName();
                        String email = assignment.getUser().getEmail();
                        return firstName + " " + lastName + " (" + email + ")";
                    })
                    .collect(Collectors.toList());

                descBuilder.append("\nAssigned employees: ").append(String.join(", ", assignedUsers));
                LOGGER.info("Employee shift '{}' has {} assigned employees: {}", shift.getDescription(), assignedUsers.size(), assignedUsers);
            }

            event.getProperties().add(new Description(descBuilder.toString()));
            calendar.getComponents().add(event);
            eventCount++;
        }

        LOGGER.info("Generated {} events for employee {} from {} filtered shifts", eventCount, employeeEmail, employeeShifts.size());

        String calendarString = calendar.toString();
        LOGGER.info("Final calendar string length: {}", calendarString.length());
        LOGGER.info("Calendar string preview: {}", calendarString.substring(0, Math.min(1000, calendarString.length())));

        return calendarString;
    }

    /**
     * Generates an iCalendar (.ics) file content specifically for subscription with additional properties
     */
    @Override
    public String  generateEmployeeSubscriptionIcal(ConcreteShiftPlanIcalDto shiftPlanDto, String employeeEmail) {
        LOGGER.info("Generating employee subscription iCal for email {} from {} shifts", employeeEmail, shiftPlanDto.scheduledShifts().size());

        // Filter shifts to only include those assigned to the specified employee
        List<ScheduledShift> employeeShifts = shiftPlanDto.scheduledShifts().stream()
            .filter(shift -> {
                boolean hasAssignments = shift.getAssignments() != null && !shift.getAssignments().isEmpty();
                boolean isAssigned = hasAssignments && shift.getAssignments().stream()
                    .anyMatch(assignment -> assignment.getUser().getEmail().equals(employeeEmail));

                LOGGER.info("Shift '{}': hasAssignments={}, isAssigned={}",
                    shift.getDescription(), hasAssignments, isAssigned);

                return isAssigned;
            })
            .collect(Collectors.toList());

        LOGGER.info("Found {} shifts assigned to employee {}", employeeShifts.size(), employeeEmail);

        if (employeeShifts.isEmpty()) {
            LOGGER.warn("No shifts found for employee {}", employeeEmail);
        }

        Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId("-//Shyft//iCal4j 3.2.10//EN"));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);

        // Add subscription-specific properties
        calendar.getProperties().add(new XProperty("X-WR-CALNAME", "My Shifts - " + employeeEmail));
        calendar.getProperties().add(new XProperty("X-WR-CALDESC", "My work shifts from Shyft"));
        calendar.getProperties().add(new XProperty("X-PUBLISHED-TTL", "PT1H")); // Refresh every hour
        calendar.getProperties().add(new XProperty("X-WR-TIMEZONE", ZoneId.systemDefault().getId()));

        int eventCount = 0;
        for (ScheduledShift shift : employeeShifts) {
            LOGGER.info("Processing employee subscription shift: {} with {} assignments", shift.getDescription(),
                shift.getAssignments() != null ? shift.getAssignments().size() : 0);

            // Convert LocalDateTime to Date
            Date startDate = Date.from(shift.getStart().atZone(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(shift.getEnd().atZone(ZoneId.systemDefault()).toInstant());

            LOGGER.info("Shift '{}': start={}, end={}", shift.getDescription(), startDate, endDate);

            // Create event with shift description as title
            VEvent event = new VEvent(new DateTime(startDate), new DateTime(endDate), shift.getDescription());

            // Add unique ID
            Uid uid = uidGenerator.generateUid();
            event.getProperties().add(uid);

            // Build description with department name and all assigned employees
            String departmentName = shift.getDepartment() != null ? shift.getDepartment().getName() : "Unknown Department";
            StringBuilder descBuilder = new StringBuilder();
            descBuilder.append("Department: ").append(departmentName);

            // Add all assigned employees to description (not just the current employee)
            if (shift.getAssignments() != null && !shift.getAssignments().isEmpty()) {
                List<String> assignedUsers = shift.getAssignments().stream()
                    .map(assignment -> {
                        String firstName = assignment.getUser().getFirstName();
                        String lastName = assignment.getUser().getLastName();
                        String email = assignment.getUser().getEmail();
                        return firstName + " " + lastName + " (" + email + ")";
                    })
                    .collect(Collectors.toList());

                descBuilder.append("\nAssigned employees: ").append(String.join(", ", assignedUsers));
                LOGGER.info("Employee subscription shift '{}' has {} assigned employees: {}", shift.getDescription(), assignedUsers.size(), assignedUsers);
            }

            event.getProperties().add(new Description(descBuilder.toString()));
            calendar.getComponents().add(event);
            eventCount++;
        }

        LOGGER.info("Generated {} events for employee {} from {} filtered shifts", eventCount, employeeEmail, employeeShifts.size());

        String calendarString = calendar.toString();
        LOGGER.info("Final subscription calendar string length: {}", calendarString.length());
        LOGGER.info("Subscription calendar string preview: {}", calendarString.substring(0, Math.min(1000, calendarString.length())));

        return calendarString;
    }
}
