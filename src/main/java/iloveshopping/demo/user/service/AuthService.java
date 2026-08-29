package iloveshopping.demo.user.service;

import iloveshopping.demo.security.JwtProvider;
import iloveshopping.demo.user.dto.*;
import iloveshopping.demo.user.entity.PasswordResetToken;
import iloveshopping.demo.user.entity.RefreshToken;
import iloveshopping.demo.user.entity.User;
import iloveshopping.demo.user.repository.PasswordResetTokenRepository;
import iloveshopping.demo.user.repository.RefreshTokenRepository;
import iloveshopping.demo.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final CaptchaService captchaService;
    private final TwoFactorAuthService twoFactorAuthService;

    @Value("${jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    @Transactional
    public void register(RegisterRequest request) {
        if (!captchaService.validateCaptcha(request.recaptchaToken())) {
            throw new IllegalArgumentException("CAPTCHA verification failed");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(User.Role.CUSTOMER)
                .provider("LOCAL")
                .isTwoFactorEnabled(false)
                .build();

        userRepository.save(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        if (user.isTwoFactorEnabled()) {
            if (request.code2FA() == null || !twoFactorAuthService.verifyCode(user.getTwoFactorSecret(), request.code2FA())) {
                return AuthResponse.require2FA();
            }
        }

        return generateAuthTokens(user);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken token = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Refresh Token"));

        if (token.isRevoked() || token.getExpiryDate().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh Token expired or revoked");
        }

        // Rotation: анульовуємо старий токен
        token.setRevoked(true);
        refreshTokenRepository.save(token);

        return generateAuthTokens(token.getUser());
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    @Transactional
    public void requestPasswordReset(@Valid PasswordResetRequest request) {
        User user = userRepository.findByEmail(request.email()).orElse(null);
        if (user != null) {
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(token)
                    .user(user)
                    .expiryDate(Instant.now().plusSeconds(3600)) // 1 година
                    .build();
            passwordResetTokenRepository.save(resetToken);

            // Тут викликається відправка Email (наприклад, JavaMailSender)
            System.out.println("Reset link: https://iloveshopping.com/reset-password?token=" + token);
        }
    }

    @Transactional
    public void confirmPasswordReset(@Valid PasswordResetConfirmRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new IllegalArgumentException("Invalid reset token"));

        if (resetToken.getExpiryDate().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Token expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
        refreshTokenRepository.deleteByUser(user); // Відкликаємо всі сесії після зміни пароля
    }

    private AuthResponse generateAuthTokens(User user) {
        String accessToken = jwtProvider.generateAccessToken(user.getEmail(), user.getRole().name());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshExpirationMs))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(accessToken, refreshToken.getToken(), false);
    }
}