package tgb.cryptoexchange.merchantdetails.details.payscrow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.LuckyPayProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LuckyPayOrderCreationServiceTest {

    @Mock
    private LuckyPayProperties luckyPayProperties;

    @InjectMocks
    private LuckyPayOrderCreationService service;

    @Test
    void getMerchantShouldReturnLuckyPay() {
        assertEquals(Merchant.LUCKY_PAY, service.getMerchant());
    }

    @Test
    void keyFunctionShouldReturnKeyIfDetailsRequestIsNull() {
        when(luckyPayProperties.key()).thenReturn("key");
        assertEquals("key", service.keyFunction().apply(null));
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            method1,1000,53355,https://google.com,12515050,someKey1
            method2,2000,12586,https://example.com,6423525253,some-key-2
            """)
    void keyFunctionShouldAlwaysReturnKey(String method, Integer amount, Long id, String callbackUrl, Long chatId, String key) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setMethod(method);
        detailsRequest.setAmount(amount);
        detailsRequest.setId(id);
        detailsRequest.setCallbackUrl(callbackUrl);
        detailsRequest.setChatId(chatId);
        when(luckyPayProperties.key()).thenReturn(key);
        assertEquals(luckyPayProperties.key(), key);
    }
}