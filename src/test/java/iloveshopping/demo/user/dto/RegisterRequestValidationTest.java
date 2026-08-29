package iloveshopping.demo.user.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegisterRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should pass validation with valid data")
    void registerRequest_Valid() {
        RegisterRequest request = new RegisterRequest(
                "valid@example.com", "password123", "John", "Doe", "valid-captcha-token"
        );

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should fail validation on invalid email and short password")
    void registerRequest_InvalidEmailAndPassword() {
        RegisterRequest request = new RegisterRequest(
                "invalid-email", "short", "John", "Doe", "valid-captcha-token"
        );

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertEquals(2, violations.size());
    }
}