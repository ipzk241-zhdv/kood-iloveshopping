package iloveshopping.demo.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import iloveshopping.demo.user.dto.RegisterRequest;
import iloveshopping.demo.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("API: Register user successfully and persist in database")
    void register_Success() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "test@example.com", "Password123!", "Alex", "Smith", "dummy-captcha"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()); // Змінено з isCreated() на isOk()

        assertTrue(userRepository.existsByEmail("test@example.com"));
    }

    @Test
    @DisplayName("Security: Protect against SQL Injection in input fields")
    void register_PreventSqlInjection() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "test@example.com", "Password123!", "Alex' OR '1'='1", "Smith", "dummy-captcha"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()); // Змінено з isCreated() на isOk()

        userRepository.findByEmail("test@example.com").ifPresent(user ->
                assertEquals("Alex' OR '1'='1", user.getFirstName())
        );
    }
}