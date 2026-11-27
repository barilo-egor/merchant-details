package net.rcetech.md.integrations.platform.rspay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.properties.RSPayBTAccountProperties;
import net.rcetech.md.integrations.service.SignatureService;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Тест аккаунта rs-pay-bt без собственного класса. Сервис создаётся так же, как это
 * делает MerchantServiceFactory: мерчант в конструкторе, свойства — по префиксу справочника,
 * RestClient — по url группы rs-pay.
 */
@ExtendWith(MockitoExtension.class)
class RSPayBTAccountServiceTest {

    private static final String API_KEY = "rs-pay-bt-api-key";
    private static final String SECRET = "rs-pay-bt-secret";

    @Mock
    private RestClient restClient;

    @Mock
    private CallbackConfig callbackConfig;

    @Mock
    private SignatureService signatureService;

    private RSPayOrderCreationService service() {
        return new RSPayOrderCreationService(Merchant.RS_PAY_BT, restClient,
                new RSPayBTAccountProperties(API_KEY, SECRET), callbackConfig, signatureService);
    }

    @Test
    void getMerchantShouldReturnRsPayBt() {
        assertEquals(Merchant.RS_PAY_BT, service().getMerchant());
    }

    @Test
    void headersShouldUseBtApiKeyAndSecret() {
        when(signatureService.hmacSHA256(anyString(), eq(SECRET))).thenReturn("bt-signature");

        HttpHeaders headers = new HttpHeaders();
        service().headers(null, "{}").accept(headers);

        assertAll(
                () -> assertEquals(API_KEY, headers.getFirst("X-Shop-API-Key")),
                () -> assertEquals("bt-signature", headers.getFirst("X-Signature")),
                () -> assertEquals("application/json", Objects.requireNonNull(headers.get("Content-Type")).getFirst())
        );
    }
}
