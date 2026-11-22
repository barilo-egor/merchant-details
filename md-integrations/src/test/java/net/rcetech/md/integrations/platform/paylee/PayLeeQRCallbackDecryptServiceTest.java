package net.rcetech.md.integrations.platform.paylee;

import com.macasaet.fernet.Key;
import com.macasaet.fernet.Token;
import net.rcetech.md.integrations.properties.PayLeeQRProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.commons.enums.Merchant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayLeeQRCallbackDecryptServiceTest {

    private final String secret = "256-bit-shared-secret-key-for-pay-lee-qr-service";
    private final String validFernetKey = "k_6_S6_8K5Tf_T2V7z7P2S-p-q0_p8H-v_p7-R_p8T0=";
    private final String inputJson = """
            {
              "orderId": "123e4567-e89b-12d3-a456-426655440000",
              "status": "COMPLETED"
            }""";
    @Mock
    private PayLeeQRProperties payLeeQRProperties;
    @InjectMocks
    private PayLeeQRCallbackDecryptService decryptService;

    @BeforeEach
    void setUp() {
        lenient()
                .when(payLeeQRProperties.secret())
                .thenReturn(secret);
    }

    @Test
    @DisplayName("Успешная расшифровка токена PayLee QR")
    void shouldDecryptPayLeeQRTokenSuccessfully() {
        when(payLeeQRProperties.secret()).thenReturn(validFernetKey);
        Key key = new Key(validFernetKey);
        String validTokenB64 = Token.generate(key, inputJson).serialise();

        String result = decryptService.decrypt(validTokenB64);

        assertEquals(inputJson, result);
    }

    @Test
    @DisplayName("Ошибка при передаче невалидного токена")
    void shouldThrowExceptionWhenTokenIsInvalid() {
        String invalidToken = "invalid-token-content";

        assertThrows(RuntimeException.class, () -> {
            decryptService.decrypt(invalidToken);
        });
    }

    @Test
    @DisplayName("Проверка возвращаемого мерчанта")
    void shouldReturnCorrectMerchant() {
        assertEquals(Merchant.PAY_LEE_QR, decryptService.getMerchant());
    }
}
