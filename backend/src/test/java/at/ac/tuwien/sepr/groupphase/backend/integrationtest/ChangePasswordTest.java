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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
        if (!roleRepository.existsById("USER")) {
            roleRepository.save(new ApplicationRole("USER"));
        }
        if (!roleRepository.existsById("ADMIN")) {
            roleRepository.save(new ApplicationRole("ADMIN"));
        }

        ApplicationUser user = new ApplicationUser(NORMAL_USER_EMAIL, passwordEncoder.encode("UserPass123"));
        user.getRoles().add(roleRepository.getReferenceById("USER"));
        userRepository.save(user);

        ApplicationUser admin = new ApplicationUser(ADMIN_USER_EMAIL, passwordEncoder.encode("AdminPass123"));
        admin.getRoles().add(roleRepository.getReferenceById("ADMIN"));
        userRepository.save(admin);
    }

    @AfterEach
    public void cleanUpUsers() {
        userRepository.deleteById(NORMAL_USER_EMAIL);
        userRepository.deleteById(ADMIN_USER_EMAIL);
    }


    @Test
    @WithMockUser(username = TestData.NORMAL_USER_EMAIL, roles = {"USER"})
    public void whenValidPasswordAsUser_thenReturns200() throws Exception {
        mockMvc.perform(put("/api/users/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"newPassword\": \"StrongPass1\"}"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = TestData.NORMAL_USER_EMAIL, roles = {"USER"})
    public void whenWeakPasswordAsUser_thenReturns400() throws Exception {
        mockMvc.perform(put("/api/users/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"newPassword\": \"abc\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = TestData.NORMAL_USER_EMAIL, roles = {"USER"})
    public void whenMissingPasswordAsUser_thenReturns400() throws Exception {
        mockMvc.perform(put("/api/users/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void whenNotAuthenticated_thenReturns403() throws Exception {
        mockMvc.perform(put("/api/users/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"newPassword\": \"StrongPass1\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = ADMIN_USER_EMAIL, roles = {"ADMIN"})
    public void whenAdminTriesToChangePassword_thenReturns403() throws Exception {
        mockMvc.perform(put("/api/users/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"newPassword\": \"StrongPass1\"}"))
            .andExpect(status().isForbidden());
    }
}
