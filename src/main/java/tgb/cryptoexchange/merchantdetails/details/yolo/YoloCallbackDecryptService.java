package tgb.cryptoexchange.merchantdetails.details.yolo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.CallbackDecryptService;
import tgb.cryptoexchange.merchantdetails.exception.CryptoException;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

@Service
@Slf4j
public class YoloCallbackDecryptService implements CallbackDecryptService {

    private final CallbackConfig callbackConfig;

    public YoloCallbackDecryptService(CallbackConfig callbackConfig) {
        this.callbackConfig = callbackConfig;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.YOLO;
    }

    public String decrypt(String jsonString) {
        String secret = callbackConfig.getCallbackSecret();

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonString);

            String base64Data = root.get("data").asText();
            byte[] combined = Base64.getDecoder().decode(base64Data);

            byte[] nonce = Arrays.copyOfRange(combined, 0, 12);
            byte[] encryptedData = Arrays.copyOfRange(combined, 12, combined.length);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(secret.getBytes(StandardCharsets.UTF_8));

            byte[] aesKey = Arrays.copyOf(keyBytes, 16);

            SecretKeySpec key = new SecretKeySpec(aesKey, "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, nonce);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

            byte[] decrypted = cipher.doFinal(encryptedData);

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException | JsonProcessingException e) {
            long currentTime = System.currentTimeMillis();
            log.error("{} Ошибка при парсинге YOLO callback {}: {}", currentTime, jsonString, e.getMessage(), e);
            throw new CryptoException("Error occurred while parsing YOLO callback: " + currentTime + ".", e);
        }
    }
}
