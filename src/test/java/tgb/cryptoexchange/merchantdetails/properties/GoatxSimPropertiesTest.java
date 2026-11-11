package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GoatxSimPropertiesTest extends AbstractMySqlTest {

    @Autowired
    private GoatxSimProperties goatxProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("goatx-sim-url", goatxProperties.url()),
                () -> assertEquals("goatx-sim-api-key", goatxProperties.apiKey()),
                () -> assertEquals("goatx-sim-merchant-id", goatxProperties.merchantId()),
                () -> assertEquals("goatx-sim-merchant-contract-id", goatxProperties.merchantContractId()),
                () -> assertEquals("goatx-sim-login", goatxProperties.login())
        );
    }
}