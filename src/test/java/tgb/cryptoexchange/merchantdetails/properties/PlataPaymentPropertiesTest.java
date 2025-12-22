package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PlataPaymentPropertiesTest {

    @Autowired
    private PlataPaymentProperties plataPaymentProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("plata-payment-url", plataPaymentProperties.url()),
                () -> assertEquals("plata-payment-token", plataPaymentProperties.token()),
                () -> assertEquals("plata-payment-merchant-id", plataPaymentProperties.merchantId())
        );
    }
}