package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class SmackayPropertiesTest {

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