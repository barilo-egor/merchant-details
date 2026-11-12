package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PayLeePropertiesImplTest extends AbstractIntegrationsContextTest {

    @Autowired
    private PayLeePropertiesImpl payLeeProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("pay-lee-url", payLeeProperties.url()),
                () -> assertEquals("pay-lee-token", payLeeProperties.token()),
                () -> assertEquals("pay-lee-secret", payLeeProperties.secret()),
                () -> assertEquals("pay-lee-client-id-salt", payLeeProperties.clientIdSalt())
        );
    }
}