package com.google.ae.integration_gateway.security;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
public class HmacValidator {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    /**
     * Validates incoming HTTP signature against calculated HMAC SHA-256 signature.
     * Uses constant-time comparison to prevent timing attacks.
     */
    public boolean isValidSignature(String payload, String signatureHeader, String secret) {
        if (payload == null || signatureHeader == null || secret == null) {
            return false;
        }

        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(secretKey);

            byte[] hashBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String calculatedHex = HexFormat.of().formatHex(hashBytes);

            // Strip prefix if header comes formatted as "sha256=..."
            String cleanIncomingSignature = signatureHeader.replace("sha256=", "").trim();

            // Constant-time array comparison to prevent timing side-channel exploits
            return MessageDigest.isEqual(
                    calculatedHex.getBytes(StandardCharsets.UTF_8),
                    cleanIncomingSignature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            return false;
        }
    }
}