package tgb.cryptoexchange.merchantdetails.details.yolo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.exception.CryptoException;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class YoloCallbackDecryptServiceTest {

    private final String secret = "test-secret-key-123";
    private final String inputJson = """
            {
              "orderId": "123e4567-e89b-12d3-a456-426655440000",
              "iternalId": "external-deal-id",
              "status": "COMPLETED",
              "reconciliationSum": 1000.00000000,
              "reconciliationAmount": 10.56462035,
              "reconciliationRate": 85.11
            }""";
    @Mock
    private CallbackConfig callbackConfig;
    @InjectMocks
    private YoloCallbackDecryptService decryptService;

    @BeforeEach
    void setUp() {
        lenient()
                .when(callbackConfig.getCallbackSecret())
                .thenReturn(secret);
    }

    @Test
    @DisplayName("Успешная расшифровка JSON с данными заказа")
    void shouldDecryptOrderJsonSuccessfully() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = Arrays.copyOf(digest.digest(secret.getBytes(StandardCharsets.UTF_8)), 16);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

        byte[] nonce = new byte[12];
        new SecureRandom().nextBytes(nonce);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, nonce);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
        byte[] encryptedBytes = cipher.doFinal(inputJson.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[nonce.length + encryptedBytes.length];
        System.arraycopy(nonce, 0, combined, 0, nonce.length);
        System.arraycopy(encryptedBytes, 0, combined, nonce.length, encryptedBytes.length);
        String base64Data = Base64.getEncoder().encodeToString(combined);

        String decryptedResult = decryptService.decrypt(base64Data);

        assertNotNull(decryptedResult);
        assertTrue(decryptedResult.contains("123e4567-e89b-12d3-a456-426655440000"));
        assertTrue(decryptedResult.contains("COMPLETED"));
        assertEquals(inputJson, decryptedResult);
    }

    @Test
    @DisplayName("Выброс CryptoException при поврежденных данных")
    void shouldThrowCryptoExceptionWhenDataIsInvalid() {
        String corruptedData = Base64.getEncoder().encodeToString("short_data".getBytes());

        CryptoException exception = assertThrows(CryptoException.class, () ->
                decryptService.decrypt(corruptedData)
        );

        assertTrue(exception.getMessage().contains("Error occurred while parsing YOLO callback"));
    }

    @Test
    @DisplayName("Проверка возвращаемого мерчанта")
    void shouldReturnCorrectMerchant() {
        assertEquals(Merchant.YOLO, decryptService.getMerchant());
    }
}