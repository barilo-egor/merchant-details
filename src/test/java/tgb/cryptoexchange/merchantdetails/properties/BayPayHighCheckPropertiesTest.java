package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class BayPayHighCheckPropertiesTest {

    @Autowired
    private BayPayHighCheckProperties payProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("bay-pay-high-check-url", payProperties.url()),
                () -> assertEquals("bay-pay-high-check-api-key", payProperties.apiKey())
        );
    }
}