package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AsgardSimPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private AsgardSimProperties asgardSimProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("asgard-sim-url", asgardSimProperties.url()),
                () -> assertEquals("asgard-sim-token", asgardSimProperties.token()),
                () -> assertEquals("asgard-sim-secret", asgardSimProperties.secret()),
                () -> assertEquals("asgard-sim-merchant-id", asgardSimProperties.merchantId())
        );
    }
}