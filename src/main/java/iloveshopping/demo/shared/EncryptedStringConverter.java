package iloveshopping.demo.shared;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/** Encrypts confidential persisted text. Configure DATA_ENCRYPTION_KEY outside source control in production. */
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {
    private static final byte[] KEY = key();
    private static final byte[] IV = "shop-order-1".getBytes(StandardCharsets.UTF_8);
    @Override public String convertToDatabaseColumn(String value) {
        if (value == null) return null;
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(KEY, "AES"), new GCMParameterSpec(128, IV));
            return Base64.getEncoder().encodeToString(cipher.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) { throw new IllegalStateException("Could not encrypt sensitive data", ex); }
    }
    @Override public String convertToEntityAttribute(String value) {
        if (value == null) return null;
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(KEY, "AES"), new GCMParameterSpec(128, IV));
            return new String(cipher.doFinal(Base64.getDecoder().decode(value)), StandardCharsets.UTF_8);
        } catch (Exception ex) { throw new IllegalStateException("Could not decrypt sensitive data", ex); }
    }
    private static byte[] key() {
        try { return MessageDigest.getInstance("SHA-256").digest(System.getenv().getOrDefault("DATA_ENCRYPTION_KEY", "development-only-change-me").getBytes(StandardCharsets.UTF_8)); }
        catch (Exception ex) { throw new IllegalStateException(ex); }
    }
}
