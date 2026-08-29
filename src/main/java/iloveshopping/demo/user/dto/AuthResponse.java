package iloveshopping.demo.user.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        boolean requires2FA
) {
    public static AuthResponse require2FA() {
        return new AuthResponse(null, null, true);
    }
}