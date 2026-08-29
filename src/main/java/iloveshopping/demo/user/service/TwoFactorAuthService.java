package iloveshopping.demo.user.service;

import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.apache.commons.codec.binary.Base32;

@Service
public class TwoFactorAuthService {

    public String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        Base32 base32 = new Base32();
        return base32.encodeToString(bytes);
    }

    public boolean verifyCode(String secret, String code) {
        if (secret == null || code == null) return false;
        long timeWindow = System.currentTimeMillis() / 1000 / 30;

        for (int i = -1; i <= 1; i++) {
            String generatedCode = generateTOTP(secret, timeWindow + i);
            if (generatedCode.equals(code)) {
                return true;
            }
        }
        return false;
    }

    private String generateTOTP(String secretKey, long timeIndex) {
        Base32 base32 = new Base32();
        byte[] key = base32.decode(secretKey);
        byte[] data = ByteBuffer.allocate(8).putLong(timeIndex).array();

        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(data);
            int offset = hash[hash.length - 1] & 0xF;
            long truncatedHash = 0;
            for (int i = 0; i < 4; ++i) {
                truncatedHash <<= 8;
                truncatedHash |= (hash[offset + i] & 0xFF);
            }
            truncatedHash &= 0x7FFFFFFF;
            truncatedHash %= 1000000;
            return String.format("%06d", truncatedHash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error generating TOTP", e);
        }
    }
}