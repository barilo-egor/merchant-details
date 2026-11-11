package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PlataPaymentPropertiesTest extends AbstractMySqlTest {

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