package at.ac.tuwien.sepr.groupphase.backend.runner;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Department;
import at.ac.tuwien.sepr.groupphase.backend.entity.PlanBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftDayBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.VacationRequest;
import at.ac.tuwien.sepr.groupphase.backend.repository.DepartmentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PlanBlueprintRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.VacationRequestRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.DepartmentService;
import at.ac.tuwien.sepr.groupphase.backend.service.shift.ShiftPlanningService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.department.DepartmentCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.Role;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.UserDataDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.UserRoleDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftDayDto;
import at.ac.tuwien.sepr.groupphase.backend.service.mapper.DepartmentMapper;
import at.ac.tuwien.sepr.groupphase.backend.type.VacationStatus;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
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
    private final VacationRequestRepository vacationRequestRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Value("${admin.user.password}")
    private String adminUserPassword;

    public StartupRunner(UserService userService, ShiftPlanningService shiftPlanningService,
                         DepartmentService departmentService, PlanBlueprintRepository planBlueprintRepository,
                         UserRepository userRepository, DepartmentRepository departmentRepository,
                         VacationRequestRepository vacationRequestRepository) {
        this.userService = userService;
        this.shiftPlanningService = shiftPlanningService;
        this.departmentService = departmentService;
        this.planBlueprintRepository = planBlueprintRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.vacationRequestRepository = vacationRequestRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        LOGGER.trace("run({})", String.join(", ", args));

        this.userService.createOrChangePassword(
            new UserDataDto(ADMIN_EMAIL, adminUserPassword, "Admin", "Shyft"));
        this.userService.assignRoleToUser(new UserRoleDto(ADMIN_EMAIL, Role.ADMIN, null));

        var production = this.departmentService.getDepartmentByName("Produktion")
            .flatMap(d -> departmentRepository.findById(d.name()));

        if (production.isEmpty()) {
            this.userService.createUser(new UserDataDto(
                "supervisor@shyft.local",
                "password",
                "Supervisor",
                "Shyft"
            ));

            LOGGER.info("Department not found, creating department!");
            var dep = this.departmentService.createDepartment(new DepartmentCreateDto("Produktion", "supervisor@shyft.local"));
            var actDep = this.departmentRepository.findById(dep.getName()).orElseThrow(() -> new RuntimeException("Department not found"));
            createPlan(actDep);
            createSecondPlan(actDep);
            createUsers(actDep);
            createVacations(actDep);
        } else {
            LOGGER.info("Department found: {}", production.get().getName());
            if (production.get().getPlans().isEmpty()) {
                LOGGER.info("Department does not have a plan!");
                createPlan(production.get());
                createSecondPlan(production.get());
                createVacations(production.get());
            } else {
                LOGGER.info("Department has a plan!");
            }
        }
    }

    private void createUsers(Department production) {
        LOGGER.info("Creating default users");
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
        this.userService.assignRoleToUser(new UserRoleDto("new_supervisor@shyft.local", Role.SUPERVISOR, secondDepartment.getName()));

        // Neue Employees für Produktion
        this.userService.createUser(new UserDataDto("employee1@shyft.local", "password", "Firstname", "Lastname"));
        this.userService.createUser(new UserDataDto("employee2@shyft.local", "password", "Firstname", "Lastname"));
        this.userService.createUser(new UserDataDto("employee3@shyft.local", "password", "Firstname", "Lastname"));
        this.userService.createUser(new UserDataDto("employee4@shyft.local", "password", "Firstname", "Lastname"));
        this.userService.createUser(new UserDataDto("employee5@shyft.local", "password", "Firstname", "Lastname"));

        // Neue Jumper für Produktion
        this.userService.createUser(new UserDataDto("jumper1@shyft.local", "password", "Jum", "Per"));
        this.userService.createUser(new UserDataDto("jumper2@shyft.local", "password", "Jum", "Per"));

        // Supervisor
        ApplicationUser supervisor = this.userRepository.findByEmail("supervisor@shyft.local").orElseThrow();
        supervisor.setDepartment(production);
        this.userRepository.save(supervisor);
        this.userService.assignRoleToUser(
            new UserRoleDto("supervisor@shyft.local", Role.SUPERVISOR, production.getName())
        );

        // Neue Employees zuweisen
        for (String email : List.of("employee1@shyft.local", "employee2@shyft.local", "employee3@shyft.local",
            "employee4@shyft.local", "employee5@shyft.local")) {
            ApplicationUser user = this.userRepository.findByEmail(email).orElseThrow();
            user.setDepartment(production);
            this.userRepository.save(user);
            this.userService.assignRoleToUser(new UserRoleDto(email, Role.EMPLOYEE, production.getName()));
        }

        // Neue Jumper zuweisen
        for (String email : List.of("jumper1@shyft.local", "jumper2@shyft.local")) {
            ApplicationUser user = this.userRepository.findByEmail(email).orElseThrow();
            user.setDepartment(production);
            this.userRepository.save(user);
            this.userService.assignRoleToUser(new UserRoleDto(email, Role.JUMPER, production.getName()));
        }

        LOGGER.info("Default users created and assigned to production department");
    }

    private void createPlan(Department department) {
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
                .build());

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

    private void createVacations(Department department) {
        VacationRequest vacationRequest1 = new VacationRequest();
        VacationRequest vacationRequest2 = new VacationRequest();

        var employee1 = this.userRepository.findByEmail("employee4@shyft.local");
        var employee2 = this.userRepository.findByEmail("employee5@shyft.local");

        vacationRequest1.setEmployee(employee1.orElseThrow(() ->
            new RuntimeException("Startuprunner: Employee not found")));
        vacationRequest2.setEmployee(employee2.orElseThrow(() ->
            new RuntimeException("Startuprunner: Employee not found")));

        vacationRequest1.setStartDate(LocalDate.of(2025, 7, 9));
        vacationRequest1.setEndDate(LocalDate.of(2025, 7, 11));
        vacationRequest1.setStatus(VacationStatus.APPROVED);

        vacationRequest2.setStartDate(LocalDate.of(2025, 7, 7));
        vacationRequest2.setEndDate(LocalDate.of(2025, 7, 10));
        vacationRequest2.setStatus(VacationStatus.APPROVED);

        this.vacationRequestRepository.save(vacationRequest1);
        this.vacationRequestRepository.save(vacationRequest2);
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
                .build());

        List<ShiftDayBlueprint> earlyShiftDays2 = List.of(
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
                .build());

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
                .build());
        List<ShiftDayBlueprint> lateShiftDays2 = List.of(
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
                .build());

        var plan = new PlanBlueprint.Builder()
            .withDepartment(department)
            .withDescription("Second plan: Early/Late Shift Schedule")
            .addShift(shiftBuilder -> {
                shiftBuilder.withDescription("Early Shift")
                    .withManPower(3)
                    .addWeek(0, weekBuilder -> weekBuilder.withDays(earlyShiftDays))
                    .addWeek(1, weekBuilder -> weekBuilder.withDays(earlyShiftDays2));
            })
            .addShift(shiftBuilder -> {
                shiftBuilder.withDescription("Late Shift")
                    .withManPower(2)
                    .addWeek(0, weekBuilder -> weekBuilder.withDays(lateShiftDays))
                    .addWeek(1, weekBuilder -> weekBuilder.withDays(lateShiftDays2));
            })
            .build();

        plan = this.planBlueprintRepository.save(plan);
    }

}