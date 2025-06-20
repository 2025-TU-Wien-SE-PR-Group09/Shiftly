package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.InvitationToken;
import at.ac.tuwien.sepr.groupphase.backend.repository.ApplicationRoleRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.InvitationTokenRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RegistrationTest implements TestData {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InvitationTokenRepository invitationTokenRepository;

    @Autowired
    private ApplicationRoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "TestPass123";
    private static final String TEST_FIRST_NAME = "Test";
    private static final String TEST_LAST_NAME = "User";

    @BeforeEach
    public void setUp() {
        // Ensure roles exist in DB
        if (!roleRepository.existsById("USER")) {
            roleRepository.save(new ApplicationRole("USER"));
        }
        if (!roleRepository.existsById("ADMIN")) {
            roleRepository.save(new ApplicationRole("ADMIN"));
        }

        // Clean up any existing test data
        cleanupTestData();
    }

    @AfterEach
    public void cleanUp() {
        cleanupTestData();
    }

    private void cleanupTestData() {
        userRepository.deleteById(TEST_EMAIL);
        invitationTokenRepository.deleteAll();
    }

    private void createUser(String email, String password, String role, String firstName, String lastName) {
        ApplicationUser user = new ApplicationUser();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.getRoles().add(roleRepository.getReferenceById(role));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        userRepository.save(user);
    }

    private InvitationToken createValidToken(String email) {
        String tokenValue = jwtTokenizer.generateInvitationToken(email);
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(24);
        InvitationToken token = new InvitationToken(tokenValue, email, expiryDate);
        return invitationTokenRepository.save(token);
    }

    /**
     * Tests successful user invitation by admin
     */
    @Test
    @WithMockUser(username = ADMIN_USER_EMAIL, roles = {"ADMIN"})
    public void whenAdminInvitesUser_thenInvitationIsCreated() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of("email", TEST_EMAIL));

        mockMvc.perform(post("/api/v1/registration/invite")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Invitation sent to " + TEST_EMAIL)));

        // Verify token was created in database
        assertTrue(invitationTokenRepository.findByEmail(TEST_EMAIL).isPresent());
    }

    /**
     * Tests that non-admin users cannot invite users
     */
    @Test
    @WithMockUser(username = NORMAL_USER_EMAIL, roles = {"EMPLOYEE"})
    public void whenNonAdminInvitesUser_thenReturns403() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of("email", TEST_EMAIL));

        mockMvc.perform(post("/api/v1/registration/invite")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isForbidden());
    }

    /**
     * Tests that unauthenticated users cannot invite users
     */
    @Test
    public void whenUnauthenticatedUserInvitesUser_thenReturns403() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of("email", TEST_EMAIL));

        mockMvc.perform(post("/api/v1/registration/invite")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isForbidden());
    }

    /**
     * Tests that inviting a user with existing email returns error
     */
    @Test
    @WithMockUser(username = ADMIN_USER_EMAIL, roles = {"ADMIN"})
    public void whenInvitingExistingUser_thenReturns400() throws Exception {
        // Create existing user
        createUser(TEST_EMAIL, TEST_PASSWORD, "USER", TEST_FIRST_NAME, TEST_LAST_NAME);

        String requestBody = objectMapper.writeValueAsString(Map.of("email", TEST_EMAIL));

        mockMvc.perform(post("/api/v1/registration/invite")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("User with this email already exists")));
    }

    /**
     * Tests that registration with mismatched email fails
     */
    @Test
    public void whenEmailMismatch_thenRegistrationFails() throws Exception {
        InvitationToken token = createValidToken(TEST_EMAIL);

        Map<String, String> userData = Map.of(
            "email", "different@example.com", // Different email than token
            "password", TEST_PASSWORD,
            "firstName", TEST_FIRST_NAME,
            "lastName", TEST_LAST_NAME
        );

        String requestBody = objectMapper.writeValueAsString(userData);

        mockMvc.perform(post("/api/v1/registration/register-with-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .param("arg1", token.getToken()))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Email address does not match the invitation token")));
    }

    /**
     * Tests that registration with invalid user data fails validation
     */
    @Test
    public void whenInvalidUserData_thenRegistrationFails() throws Exception {
        InvitationToken token = createValidToken(TEST_EMAIL);

        // Invalid data - missing required fields
        Map<String, String> userData = Map.of(
            "email", TEST_EMAIL
            // Missing password, firstName, lastName
        );

        String requestBody = objectMapper.writeValueAsString(userData);

        mockMvc.perform(post("/api/v1/registration/register-with-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .param("arg1", token.getToken()))
            .andExpect(status().isBadRequest());
    }

    /**
     * Tests that registration with weak password fails validation
     */
    @Test
    public void whenWeakPassword_thenRegistrationFails() throws Exception {
        InvitationToken token = createValidToken(TEST_EMAIL);

        Map<String, String> userData = Map.of(
            "email", TEST_EMAIL,
            "password", "weak", // Too short
            "firstName", TEST_FIRST_NAME,
            "lastName", TEST_LAST_NAME
        );

        String requestBody = objectMapper.writeValueAsString(userData);

        mockMvc.perform(post("/api/v1/registration/register-with-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .param("arg1", token.getToken()))
            .andExpect(status().isBadRequest());
    }

    /**
     * Tests that registration with invalid email format fails validation
     */
    @Test
    public void whenInvalidEmailFormat_thenRegistrationFails() throws Exception {
        InvitationToken token = createValidToken(TEST_EMAIL);

        Map<String, String> userData = Map.of(
            "email", "invalid-email", // Invalid email format
            "password", TEST_PASSWORD,
            "firstName", TEST_FIRST_NAME,
            "lastName", TEST_LAST_NAME
        );

        String requestBody = objectMapper.writeValueAsString(userData);

        mockMvc.perform(post("/api/v1/registration/register-with-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .param("arg1", token.getToken()))
            .andExpect(status().isBadRequest());
    }
}