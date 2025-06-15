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
import java.util.ArrayList;
import java.util.Arrays;
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

        var production = this.departmentService.getDepartmentByName("Production")
            .flatMap(d -> departmentRepository.findById(d.name()));

        if (production.isEmpty()) {
            for (int i = 1; i <= 2; i++) {
                this.userService.createUser(new UserDataDto(
                    "supervisor"+i+"@shyft.local",
                    "password",
                    "Supervisor",
                    "Shyft"+i
                ));
            }

            LOGGER.info("Department not found, creating department!");
            var dep = this.departmentService.createDepartment(new DepartmentCreateDto("Production", "supervisor1@shyft.local"));
            var dep2 = this.departmentService.createDepartment(new DepartmentCreateDto("Customer Support", "supervisor2@shyft.local"));
            var actDep = this.departmentRepository.findById(dep.getName()).orElseThrow(() -> new RuntimeException("Department not found"));
            var actDep2 = this.departmentRepository.findById(dep2.getName()).orElseThrow(() -> new RuntimeException("Department not found"));
            createPlan(actDep);
            createPlan(actDep2);
            createSecondPlan(actDep);
            createSecondPlan(actDep2);
            createUsers(actDep, "supervisor1@shyft.local",1);
            createUsers(actDep2, "supervisor2@shyft.local", 10);
            createVacations();
        } else {
            LOGGER.info("Department found: {}", production.get().getName());
            if (production.get().getPlans().isEmpty()) {
                LOGGER.info("Department does not have a plan!");
                createPlan(production.get());
                createSecondPlan(production.get());
                createVacations();
            } else {
                LOGGER.info("Department has a plan!");
            }
        }
    }

    private void createUsers(Department production, String supervisorMail, int startC) {
        // Neue Employees für Production
        for (int i = startC; i < startC+5; i++) {
            String email = "employee"+i+"@shyft.local";
            this.userService.createUser(new UserDataDto(email, "password", "Firstname", "Lastname"));

            ApplicationUser user = this.userRepository.findByEmail(email).orElseThrow();
            user.setDepartment(production);
            this.userRepository.save(user);
            this.userService.assignRoleToUser(new UserRoleDto(email, Role.EMPLOYEE, production.getName()));
        }

        // Neue Jumper für Production
        for (int i = startC; i < startC+2; i++) {
            String email = "jumper"+i+"@shyft.local";
            this.userService.createUser(new UserDataDto(email, "password", "Jum", "Per"));

            ApplicationUser user = this.userRepository.findByEmail(email).orElseThrow();
            user.setDepartment(production);
            this.userRepository.save(user);
            this.userService.assignRoleToUser(new UserRoleDto(email, Role.JUMPER, production.getName()));
        }

        // Supervisor
        ApplicationUser supervisor = this.userRepository.findByEmail(supervisorMail).orElseThrow();
        supervisor.setDepartment(production);
        this.userRepository.save(supervisor);
        this.userService.assignRoleToUser(
            new UserRoleDto(supervisorMail, Role.SUPERVISOR, production.getName())
        );

        LOGGER.info("Default users created and assigned to production department");
    }



    private void createPlan(Department department) {
        List<ShiftDayBlueprint> days = getListOfSameShiftdayDuringWeek(
            LocalTime.of(7, 0),
            Duration.ofHours(8));

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

    private List<ShiftDayBlueprint> getListOfSameShiftdayBlueprint(LocalTime startTime, Duration duration, List<DayOfWeek> days) {
        List<ShiftDayBlueprint> earlyShiftDays = new ArrayList<>();

        for (DayOfWeek day : days) {
            earlyShiftDays.add(
                new ShiftDayBlueprint.Builder().withDay(day)
                    .withStartTime(startTime)
                    .withDuration(duration)
                    .build()
            );
        }

        return earlyShiftDays;
    }

    private List<ShiftDayBlueprint> getListOfSameShiftdayDuringWeek(LocalTime startTime, Duration duration) {
        return this.getListOfSameShiftdayBlueprint(startTime, duration,
            List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));
    }

    private void createVacations() {
        createVacation("employee4@shyft.local",
            LocalDate.now().plusWeeks(5),
            LocalDate.now().plusWeeks(6));
        createVacation("employee5@shyft.local",
            LocalDate.now().plusWeeks(5).plusDays(1),
            LocalDate.now().plusWeeks(6).plusDays(1));

        createVacation("employee10@shyft.local",
            LocalDate.now().plusWeeks(5),
            LocalDate.now().plusWeeks(6));
        createVacation("employee11@shyft.local",
            LocalDate.now().plusWeeks(5).plusDays(1),
            LocalDate.now().plusWeeks(6).plusDays(1));
        createVacation("employee12@shyft.local",
            LocalDate.now().plusWeeks(5).plusDays(2),
            LocalDate.now().plusWeeks(6).plusDays(2));
        createVacation("employee13@shyft.local",
            LocalDate.now().plusWeeks(5).plusDays(3),
            LocalDate.now().plusWeeks(6).plusDays(3));
    }

    private void createVacation(String employeeMail, LocalDate from, LocalDate to) {
        VacationRequest vacationRequest = new VacationRequest();

        var employee = this.userRepository.findByEmail(employeeMail);

        vacationRequest.setEmployee(employee.orElseThrow(() ->
            new RuntimeException("Startuprunner: Employee not found")));

        vacationRequest.setStartDate(from);
        vacationRequest.setEndDate(to);
        vacationRequest.setStatus(VacationStatus.APPROVED);

        this.vacationRequestRepository.save(vacationRequest);
    }

    private void createSecondPlan(Department department) {
        List<ShiftDayBlueprint> earlyShiftDays = getListOfSameShiftdayDuringWeek(
            LocalTime.of(5, 30),
            Duration.ofHours(8));

        List<ShiftDayBlueprint> earlyShiftDays2 = getListOfSameShiftdayDuringWeek(
            LocalTime.of(5, 30),
            Duration.ofHours(8));

        List<ShiftDayBlueprint> lateShiftDays = getListOfSameShiftdayDuringWeek(
            LocalTime.of(14, 0),
            Duration.ofHours(8));

        List<ShiftDayBlueprint> lateShiftDays2 = getListOfSameShiftdayDuringWeek(
            LocalTime.of(14, 0),
            Duration.ofHours(8));


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