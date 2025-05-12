package at.ac.tuwien.sepr.groupphase.backend.runner;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.Role;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserDataDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserRoleDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

@Component
public class StartupRunner implements CommandLineRunner {
    private final UserService userService;
    private final String adminUsername = "admin@shyft.local";

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Value("admin.user.password")
    private String adminUserPassword;

    public StartupRunner(UserService userService) {
        this.userService = userService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        LOGGER.trace("run({})", String.join(", ", args));

        ApplicationUser adminUser = this.userService.createOrChangePassword(
            new UserDataDto(adminUsername, adminUserPassword)
        );

        this.userService.assignRoleToUser(new UserRoleDto(adminUser.getEmail(), Role.ADMIN));
    }
}