package iloveshopping.demo.user.dto;

public record TwoFactorSetupResponse(String secret, String qrCodeUrl) {}