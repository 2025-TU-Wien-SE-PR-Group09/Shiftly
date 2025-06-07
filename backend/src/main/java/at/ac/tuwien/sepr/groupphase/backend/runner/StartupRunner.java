package at.ac.tuwien.sepr.groupphase.backend.runner;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Department;
import at.ac.tuwien.sepr.groupphase.backend.entity.PlanBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftDayBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.repository.DepartmentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PlanBlueprintRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.DepartmentService;
import at.ac.tuwien.sepr.groupphase.backend.service.ShiftPlanningService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.department.DepartmentCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.Role;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.UserDataDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.UserRoleDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftDayDto;
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
    private final PlanBlueprintRepository planBlueprintRepository;

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Value("${admin.user.password}")
    private String adminUserPassword;

    public StartupRunner(UserService userService, ShiftPlanningService shiftPlanningService,
                         DepartmentService departmentService, PlanBlueprintRepository planBlueprintRepository, UserRepository userRepository, DepartmentRepository departmentRepository) {
        this.userService = userService;
        this.shiftPlanningService = shiftPlanningService;
        this.departmentService = departmentService;
        this.planBlueprintRepository = planBlueprintRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        LOGGER.trace("run({})", String.join(", ", args));

        this.userService.createOrChangePassword(
            new UserDataDto(ADMIN_EMAIL, adminUserPassword, "Admin", "Shyft"));
        this.userService.assignRoleToUser(new UserRoleDto(ADMIN_EMAIL, Role.ADMIN, null));

        var production = this.departmentService.getDepartmentByName("Produktion")
            .flatMap(d -> departmentRepository.findById(d.id()));

        if (production.isEmpty()) {
            this.userService.createUser(new UserDataDto(
                "supervisor@shyft.local",
                "password",
                "Supervisor",
                "Shyft"
            ));

            LOGGER.info("Department not found, creating department!");
            var dep = this.departmentService.createDepartment(new DepartmentCreateDto("Produktion", "supervisor@shyft.local"));
            var actDep = this.departmentRepository.findById(dep.getId()).orElseThrow(() -> new RuntimeException("Department not found"));
            createPlan(actDep);
            createSecondPlan(actDep);
            createUsers();
        } else {
            LOGGER.info("Department found: {}", production.get().getName());
            if (production.get().getPlans().isEmpty()) {
                LOGGER.info("Department does not have a plan!");
                createPlan(production.get());
                createSecondPlan(production.get());
            } else {
                LOGGER.info("Department has a plan!");
            }
        }
    }

    private void createUsers() {
        LOGGER.info("Creating default users");
        this.userService.createUser(new UserDataDto(
            "employee@shyft.local",
            "password",
            "Employee",
            "Shyft"
        ));
        this.userService.createUser(new UserDataDto(
            "new_account@shyft.local",
            "password",
            "NewAccount",
            "Shyft"
        ));
        this.userService.createUser(new UserDataDto(
            "new_supervisor@shyft.local",
            "password",
            "NewSupervisor",
            "Shyft"
        ));

        this.departmentService.createDepartment(new DepartmentCreateDto("Controlling", "new_supervisor@shyft.local"));
        var secondDepartment = this.departmentRepository.findByName("Controlling")
            .orElseThrow(() -> new RuntimeException("Department Controlling not found"));
        var newSupervisor = this.userRepository.findByEmail("new_supervisor@shyft.local").get();
        newSupervisor.setDepartment(secondDepartment);
        this.userRepository.save(newSupervisor);
        this.userService.assignRoleToUser(new UserRoleDto("new_supervisor@shyft.local", Role.SUPERVISOR, secondDepartment.getId()));

        ApplicationUser supervisor = this.userRepository.findByEmail("supervisor@shyft.local").get();
        var department = this.departmentRepository.findByName("Produktion").get();
        supervisor.setDepartment(department);
        this.userRepository.save(supervisor);
        this.userService.assignRoleToUser(
            new UserRoleDto("supervisor@shyft.local", Role.SUPERVISOR, department.getId())
        );

        ApplicationUser employee = this.userRepository.findByEmail("employee@shyft.local").get();
        employee.setDepartment(this.departmentRepository.findByName("Produktion").get());
        this.userRepository.save(employee);
        this.userService.assignRoleToUser(
            new UserRoleDto("employee@shyft.local", Role.EMPLOYEE, department.getId())
        );

        /*
        this.userService.assignRoleToUser(
            new UserRoleDto("new_supervisor@shyft.local", Role.SUPERVISOR, department.getId())
        );*/
    }

    private void createPlan(Department department) {
        var monDay = new ShiftDayDto(DayOfWeek.MONDAY, LocalTime.of(7, 0), Duration.ofHours(8));
        var tuesDay = new ShiftDayDto(DayOfWeek.TUESDAY, LocalTime.of(7, 0), Duration.ofHours(8));
        var wedDay = new ShiftDayDto(DayOfWeek.WEDNESDAY, LocalTime.of(7, 0), Duration.ofHours(8));
        var thurDay = new ShiftDayDto(DayOfWeek.THURSDAY, LocalTime.of(7, 0), Duration.ofHours(8));
        var fridayDay = new ShiftDayDto(DayOfWeek.FRIDAY, LocalTime.of(7, 0), Duration.ofHours(8));

        List<ShiftDayBlueprint> days = List.of(
            new ShiftDayBlueprint.Builder().withDay(DayOfWeek.MONDAY)
                .withStartTime(LocalTime.of(7, 0))
                .withDuration(Duration.ofHours(8))
                .build(),
            new ShiftDayBlueprint.Builder().withDay(DayOfWeek.TUESDAY)
                .withStartTime(LocalTime.of(7, 0))
                .withDuration(Duration.ofHours(8))
                .build(),
            new ShiftDayBlueprint.Builder().withDay(DayOfWeek.WEDNESDAY)
                .withStartTime(LocalTime.of(7, 0))
                .withDuration(Duration.ofHours(8))
                .build(),
            new ShiftDayBlueprint.Builder().withDay(DayOfWeek.THURSDAY)
                .withStartTime(LocalTime.of(7, 0))
                .withDuration(Duration.ofHours(8))
                .build(),
            new ShiftDayBlueprint.Builder()
                .withDay(DayOfWeek.FRIDAY)
                .withStartTime(LocalTime.of(7, 0))
                .withDuration(Duration.ofHours(8))
                .build()
        );

        var plan = new PlanBlueprint.Builder()
            .withDepartment(department)
            .withDescription("Standard-plan for Production (1-1 Day/Night Rotation)")
            .addShift(shiftBuilder -> {
                shiftBuilder.withDescription("Dayshift")
                    .withManPower(4)
                    .addWeek(0, weekBuilder -> {
                        weekBuilder.withDays(days);
                    });
            })
            .build();
        plan = this.planBlueprintRepository.save(plan);


    }

    private void createSecondPlan(Department department) {
        List<ShiftDayBlueprint> earlyShiftDays = List.of(
            new ShiftDayBlueprint.Builder().withDay(DayOfWeek.MONDAY)
                .withStartTime(LocalTime.of(5, 30))
                .withDuration(Duration.ofHours(8))
                .build(),
            new ShiftDayBlueprint.Builder().withDay(DayOfWeek.TUESDAY)
                .withStartTime(LocalTime.of(5, 30))
                .withDuration(Duration.ofHours(8))
                .build(),
            new ShiftDayBlueprint.Builder().withDay(DayOfWeek.WEDNESDAY)
                .withStartTime(LocalTime.of(5, 30))
                .withDuration(Duration.ofHours(8))
                .build(),
            new ShiftDayBlueprint.Builder().withDay(DayOfWeek.THURSDAY)
                .withStartTime(LocalTime.of(5, 30))
                .withDuration(Duration.ofHours(8))
                .build(),
            new ShiftDayBlueprint.Builder().withDay(DayOfWeek.FRIDAY)
                .withStartTime(LocalTime.of(5, 30))
                .withDuration(Duration.ofHours(8))
                .build()
        );

        List<ShiftDayBlueprint> lateShiftDays = List.of(
            new ShiftDayBlueprint.Builder().withDay(DayOfWeek.MONDAY)
                .withStartTime(LocalTime.of(14, 0))
                .withDuration(Duration.ofHours(8))
                .build(),
            new ShiftDayBlueprint.Builder().withDay(DayOfWeek.TUESDAY)
                .withStartTime(LocalTime.of(14, 0))
                .withDuration(Duration.ofHours(8))
                .build(),
            new ShiftDayBlueprint.Builder().withDay(DayOfWeek.WEDNESDAY)
                .withStartTime(LocalTime.of(14, 0))
                .withDuration(Duration.ofHours(8))
                .build(),
            new ShiftDayBlueprint.Builder().withDay(DayOfWeek.THURSDAY)
                .withStartTime(LocalTime.of(14, 0))
                .withDuration(Duration.ofHours(8))
                .build(),
            new ShiftDayBlueprint.Builder().withDay(DayOfWeek.FRIDAY)
                .withStartTime(LocalTime.of(14, 0))
                .withDuration(Duration.ofHours(8))
                .build()
        );


        var plan = new PlanBlueprint.Builder()
            .withDepartment(department)
            .withDescription("Second plan: Early/Late Shift Schedule")
            .addShift(shiftBuilder -> {
                shiftBuilder.withDescription("Early Shift")
                    .withManPower(3)
                    .addWeek(0, weekBuilder -> weekBuilder.withDays(earlyShiftDays));
            })
            .addShift(shiftBuilder -> {
                shiftBuilder.withDescription("Late Shift")
                    .withManPower(3)
                    .addWeek(0, weekBuilder -> weekBuilder.withDays(lateShiftDays));
            })
            .build();

        plan = this.planBlueprintRepository.save(plan);
    }


}