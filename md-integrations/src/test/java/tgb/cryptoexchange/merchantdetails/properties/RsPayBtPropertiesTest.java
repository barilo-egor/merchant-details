package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RsPayBtPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private RSPayBTProperties rsPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("rs-pay-bt-api-key", rsPayProperties.apiKey()),
                () -> assertEquals("rs-pay-bt-secret", rsPayProperties.secret())
        );
    }
}