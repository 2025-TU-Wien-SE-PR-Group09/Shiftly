package at.ac.tuwien.sepr.groupphase.backend.runner;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements CommandLineRunner {
    private final UserService userService;

    private final String ADMIN_USERNAME = "admin@shyft.local";

    @Value("admin.user.password")
    private String adminUserPassword;

    public StartupRunner(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        ApplicationUser adminUser = this.userService.createOrChangePassword(ADMIN_USERNAME, adminUserPassword);
        this.userService.assignRoleToUser("ADMIN", adminUser);
    }
}