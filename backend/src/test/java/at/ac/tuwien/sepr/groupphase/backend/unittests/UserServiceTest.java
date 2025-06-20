package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.Role;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.UserDataDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.UserRoleDto;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional // this keeps the database clean
class UserServiceTest {
    @Inject
    private UserService userService;
    @Inject
    private AuthService authService;
    @Inject
    private PasswordEncoder passwordEncoder;
    @Inject
    private UserRepository userRepository; // used to verify if a user really exists

    @Test
    public void createOrChangePasswordUserDoesNotExistOK() {
        UserDataDto toUpdate = new UserDataDto(TestData.ADMIN_USER_EMAIL, TestData.ADMIN_PW, "Admin",
            "Test");
        userService.createOrChangePassword(toUpdate);
        assertUserExists(toUpdate);
    }

    @Test
    public void createOrChangePasswordUserExistsOK() {
        userService.createOrChangePassword(new UserDataDto(TestData.ADMIN_USER_EMAIL, "empty_pw",
            "Admin", "Test"));

        UserDataDto toUpdate = new UserDataDto(TestData.ADMIN_USER_EMAIL, TestData.ADMIN_PW, "Admin",
            "Test");
        userService.createOrChangePassword(toUpdate);
        assertUserExists(toUpdate);
    }

    @Test
    public void assignRoleToUserOK() {
        userService.createOrChangePassword(new UserDataDto(TestData.ADMIN_USER_EMAIL, TestData.ADMIN_PW,
            "Admin", "Test"));
        userService.assignRoleToUser(new UserRoleDto(TestData.ADMIN_USER_EMAIL, Role.ADMIN, null));

        UserDetails applicationUser = authService.loadUserByUsername(TestData.ADMIN_USER_EMAIL);
        assertUserHasRole(applicationUser, Role.ADMIN);
    }

    @Test
    public void assignRoleToUserNonexistentUser() {
        assertThrows(NotFoundException.class,
            () -> userService.assignRoleToUser(new UserRoleDto("NONEXISTENT_MAIL@test.com", Role.ADMIN, null)));
    }

    private void assertUserHasRole(UserDetails user, Role role) {
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertThat(authorities)
            .extracting(GrantedAuthority::getAuthority)
            .as("Check if user has role %s", role)
            .contains(role.name());
    }

    private void assertUserExists(UserDataDto user) {
        Optional<ApplicationUser> applicationUserOpt = userRepository.findByEmail(user.getEmail());
        assertThat(applicationUserOpt).isPresent();
        ApplicationUser applicationUser = applicationUserOpt.get();

        assertAll(
            () -> assertEquals(user.getEmail(), applicationUser.getEmail()),
            () -> assertThat(passwordEncoder.matches(user.getPassword(), applicationUser.getPasswordHash()))
                .as("Password of " + user.getEmail() + " (" + user.getPassword() + ") does not match "
                    + applicationUser.getPasswordHash())
                .isTrue());
    }
}