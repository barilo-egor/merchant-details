package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SmackayPropertiesTest extends AbstractMySqlTest {

    @Autowired
    private SmackPayProperties smackPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("smack-pay-url", smackPayProperties.url()),
                () -> assertEquals("smack-pay-api-key", smackPayProperties.apiKey())
        );
    }
}