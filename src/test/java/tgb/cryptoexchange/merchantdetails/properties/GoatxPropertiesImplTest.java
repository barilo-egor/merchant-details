package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class GoatxPropertiesImplTest {

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