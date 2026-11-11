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

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class YoloCallbackDecryptServiceTest {

    private final String secret = "cqqLQyeFkmGfVPU42ezMHz2EkVdpDhmA";

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
    void shouldDecryptOrderJsonSuccessfully() {
        String base64Data = "{\"data\":\"jgdVlgGPZIiEpQfJqw2967Wm+4UMkAfQ3n7IRqKi/Wdj2D7l5hC0gNg+6fREQaU8zyv9D1B7AJQqv0+iUqOLFpqfJTn9GRjEsmzaeIWQTrnenFAXTyyPyG/v4xEGZZe4rZQlxpLravSQj/OfkhyjPWZFKV1sj7v3yEMX5tXFoAELPVJRIjQUrTy15zBRqsd+ccmgxjfqCVQ1iVXQKQXzRciC5BOa6EmHkZ5a3Ng82VNlXHvKMPurmvcamOXaPMklT8mY/W2W587uyTkk+ohOs8hzlQVQm03rcBAqd5ip0rabShNh+gWRzSt42GAgQuY0RRto\"}";
        String decryptedResult = decryptService.decrypt(base64Data);
        assertEquals("{\"iternalId\":\"a35a297d-9e3e-4634-9e83-3c8bd196890c\",\"orderId\":\"e1d29d1a-d39f-45d0-bb77-75ca05b01645\",\"status\":\"CANCELED\",\"reconciliationSum\":null,\"reconciliationAmount\":null,\"reconciliationRate\":null,\"fileUrl\":null}", decryptedResult);
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