package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PayLeeQRPropertiesTest {

    @Autowired
    private PayLeeQRProperties payLeeProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("pay-lee-qr-url", payLeeProperties.url()),
                () -> assertEquals("pay-lee-qr-token", payLeeProperties.token()),
                () -> assertEquals("pay-lee-qr-secret", payLeeProperties.secret()),
                () -> assertEquals("pay-lee-qr-client-id-salt", payLeeProperties.clientIdSalt())
        );
    }
}