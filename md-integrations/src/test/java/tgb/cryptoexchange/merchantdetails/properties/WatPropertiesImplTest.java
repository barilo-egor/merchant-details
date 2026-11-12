package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WatPropertiesImplTest extends AbstractIntegrationsContextTest {

    @Autowired
    private WatPropertiesImpl watProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("wat-url", watProperties.url()),
                () -> assertEquals("wat-token", watProperties.token())
        );
    }
}