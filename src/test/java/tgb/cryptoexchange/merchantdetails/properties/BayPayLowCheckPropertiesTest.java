package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class BayPayLowCheckPropertiesTest {

    @Autowired
    private BayPayLowCheckProperties payProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("bay-pay-low-check-url", payProperties.url()),
                () -> assertEquals("bay-pay-low-check-api-key", payProperties.apiKey())
        );
    }
}