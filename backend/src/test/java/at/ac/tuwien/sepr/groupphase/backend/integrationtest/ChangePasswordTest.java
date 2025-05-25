package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.repository.ApplicationRoleRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ChangePasswordTest implements TestData {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ApplicationRoleRepository roleRepository;

    @BeforeEach
    public void setUpUsers() {
        // Ensure roles exist in DB
        if (!roleRepository.existsById("USER")) {
            roleRepository.save(new ApplicationRole("USER"));
        }
        if (!roleRepository.existsById("ADMIN")) {
            roleRepository.save(new ApplicationRole("ADMIN"));
        }

        createUser(NORMAL_USER_EMAIL, "UserPass123", "USER");
        createUser(ADMIN_USER_EMAIL, "AdminPass123", "ADMIN");
    }

    private void createUser(String email, String password, String role) {
        ApplicationUser user = new ApplicationUser();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.getRoles().add(roleRepository.getReferenceById(role));
        userRepository.save(user);
    }


    @AfterEach
    public void cleanUpUsers() {
        userRepository.deleteById(NORMAL_USER_EMAIL);
        userRepository.deleteById(ADMIN_USER_EMAIL);
    }

    /**
     * Tests that a valid password change as a normal user succeeds and updates the password.
     */
    @Test
    @WithMockUser(username = TestData.NORMAL_USER_EMAIL, roles = {"USER"})
    public void whenValidPasswordAsUser_thenPasswordIsUpdated() {
        String newPassword = "StrongPass1";

        assertDoesNotThrow(() -> {
            mockMvc.perform(put("/api/users/me/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"newPassword\": \"" + newPassword + "\"}"))
                .andExpect(status().isNoContent());

            ApplicationUser updatedUser = userRepository.findById(NORMAL_USER_EMAIL).orElseThrow();
            assertTrue(passwordEncoder.matches(newPassword, updatedUser.getPasswordHash()));
        });
    }

    /**
     * Tests that using a weak password results in a 400 response with validation message.
     */
    @Test
    @WithMockUser(username = TestData.NORMAL_USER_EMAIL, roles = {"USER"})
    public void whenWeakPasswordAsUser_thenReturnsValidationError() {
        assertDoesNotThrow(() -> {
            mockMvc.perform(put("/api/users/me/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"newPassword\": \"abc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                    "New password must be at least 8 characters long and include uppercase, lowercase and a digit"
                )));
        });

    }

    /**
     * Tests that omitting the password field results in a 400 response.
     */
    @Test
    @WithMockUser(username = TestData.NORMAL_USER_EMAIL, roles = {"USER"})
    public void whenMissingPasswordAsUser_thenReturns400() {
        assertDoesNotThrow(() -> {
            mockMvc.perform(put("/api/users/me/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("rawPassword cannot be null")));
        });
    }

    /**
     * Tests that unauthenticated users cannot change their password and receive 403.
     */
    @Test
    public void whenNotAuthenticated_thenReturns403() {
        assertDoesNotThrow(() -> {
            mockMvc.perform(put("/api/users/me/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"newPassword\": \"StrongPass1\"}"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Access Denied")));
        });
    }

    /**
     * Tests that an admin user is forbidden from changing their password.
     */
    @Test
    @WithMockUser(username = ADMIN_USER_EMAIL, roles = {"ADMIN"})
    public void whenAdminTriesToChangePassword_thenReturns403() {
        assertDoesNotThrow(() -> {
            mockMvc.perform(put("/api/users/me/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"newPassword\": \"StrongPass1\"}"))
                .andExpect(status().isForbidden());
        });
    }
}
