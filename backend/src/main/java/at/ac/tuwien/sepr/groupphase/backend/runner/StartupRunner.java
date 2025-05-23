package at.ac.tuwien.sepr.groupphase.backend.runner;

import at.ac.tuwien.sepr.groupphase.backend.service.DepartmentService;
import at.ac.tuwien.sepr.groupphase.backend.service.ShiftPlanningService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.DepartmentCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.Role;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.UserDataDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.UserRoleDto;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.CreateShiftBlueprintDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftDayDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftWeekBlueprintDto;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

import static at.ac.tuwien.sepr.groupphase.backend.config.Constants.ADMIN_EMAIL;

@Component
public class StartupRunner implements CommandLineRunner {
    private final UserService userService;
    private final ShiftPlanningService shiftPlanningService;
    private final DepartmentService departmentService;

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Value("${admin.user.password}")
    private String adminUserPassword;

    public StartupRunner(UserService userService, ShiftPlanningService shiftPlanningService,
                         DepartmentService departmentService) {
        this.userService = userService;
        this.shiftPlanningService = shiftPlanningService;
        this.departmentService = departmentService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        LOGGER.trace("run({})", String.join(", ", args));

        this.userService.createOrChangePassword(
            new UserDataDto(ADMIN_EMAIL, adminUserPassword));

        this.userService.assignRoleToUser(new UserRoleDto(ADMIN_EMAIL, Role.ADMIN));

        var production = this.departmentService.getDepartmentByName("Produktion");

        if (production.isEmpty()) {
            LOGGER.info("Department not found, creating department.");
            var dep = this.departmentService.createDepartment(new DepartmentCreateDto("Produktion", ADMIN_EMAIL));
            // createPlan(dep.getId());
        } else {
            LOGGER.info("Department found: {}", production.get().name());
            if (production.get().plans().isEmpty()) {
                LOGGER.info("Department does not have a plan.");
                // createPlan(production.get().id());
            } else {
                LOGGER.info("Department has a plan.");
            }
        }
    }

    private void createPlan(Long departmentId) {

        // === Tagschicht: 07:00 – 15:00 ===
        var monDay = new ShiftDayDto(DayOfWeek.MONDAY, LocalTime.of(7, 0), Duration.ofHours(8));
        var tuesDay = new ShiftDayDto(DayOfWeek.TUESDAY, LocalTime.of(7, 0), Duration.ofHours(8));
        var wedDay = new ShiftDayDto(DayOfWeek.WEDNESDAY, LocalTime.of(7, 0), Duration.ofHours(8));
        var thurDay = new ShiftDayDto(DayOfWeek.THURSDAY, LocalTime.of(7, 0), Duration.ofHours(8));
        var fridayDay = new ShiftDayDto(DayOfWeek.FRIDAY, LocalTime.of(7, 0), Duration.ofHours(8));

        var dayShift = shiftPlanningService.createShiftBlueprint(
            new CreateShiftBlueprintDto(departmentId, "Tagschicht", 4)
        );
        var a = List.of(new ShiftWeekBlueprintDto(List.of(monDay, tuesDay, wedDay, thurDay, fridayDay)));
        shiftPlanningService.addWeeksToShift(dayShift.id(), a);

        // === Nachtschicht: 18:00 – 02:00 bzw. 6.5h Fr ===
        var monNight = new ShiftDayDto(DayOfWeek.MONDAY, LocalTime.of(18, 0), Duration.ofHours(8));
        var tuesNight = new ShiftDayDto(DayOfWeek.TUESDAY, LocalTime.of(18, 0), Duration.ofHours(8));
        var wedNight = new ShiftDayDto(DayOfWeek.WEDNESDAY, LocalTime.of(18, 0), Duration.ofHours(8));
        var thurNight = new ShiftDayDto(DayOfWeek.THURSDAY, LocalTime.of(18, 0), Duration.ofHours(8));
        var saturdayNight = new ShiftDayDto(DayOfWeek.SATURDAY, LocalTime.of(18, 0), Duration.ofHours(6).plusMinutes(30));

        var nightShift = shiftPlanningService.createShiftBlueprint(
            new CreateShiftBlueprintDto(departmentId, "Nachtschicht", 4)
        );

        shiftPlanningService.addWeeksToShift(nightShift.id(),
            List.of(new ShiftWeekBlueprintDto(List.of(monNight, tuesNight, wedNight, thurNight, saturdayNight))));

        shiftPlanningService.createPlanBlueprint(departmentId, List.of(dayShift.id(), nightShift.id()));
    }

}