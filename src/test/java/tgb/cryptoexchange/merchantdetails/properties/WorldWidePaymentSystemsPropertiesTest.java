package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WorldWidePaymentSystemsPropertiesTest {

    @Autowired
    private WorldWidePaymentSystemsProperties worldWidePaymentSystemsProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("wwps-url", worldWidePaymentSystemsProperties.url()),
                () -> assertEquals("wwps-token", worldWidePaymentSystemsProperties.token()),
                () -> assertEquals("wwps-merchant-id", worldWidePaymentSystemsProperties.merchantId())
        );
    }
}