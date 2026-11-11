package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GoatxPropertiesImplTest extends AbstractMySqlTest {

    @Autowired
    private GoatxPropertiesImpl goatxProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("goatx-url", goatxProperties.url()),
                () -> assertEquals("goatx-api-key", goatxProperties.apiKey()),
                () -> assertEquals("goatx-merchant-id", goatxProperties.merchantId()),
                () -> assertEquals("goatx-merchant-contract-id", goatxProperties.merchantContractId()),
                () -> assertEquals("goatx-login", goatxProperties.login())
        );
    }
}