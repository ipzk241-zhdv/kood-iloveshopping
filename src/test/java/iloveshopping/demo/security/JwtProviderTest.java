package iloveshopping.demo.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderTest {

    private JwtProvider jwtProvider;
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(secret, 1000L);
    }

    @Test
    @DisplayName("Should return false for expired JWT token")
    void validateToken_Expired() throws InterruptedException {
        String token = jwtProvider.generateAccessToken("user@example.com", "ROLE_CUSTOMER");

        Thread.sleep(1100);

        assertFalse(jwtProvider.validateToken(token));
    }

    @Test
    @DisplayName("Should return false for invalid JWT signature")
    void validateToken_InvalidSignature() {
        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.invalidPayload.invalidSignature";
        assertFalse(jwtProvider.validateToken(invalidToken));
    }
}