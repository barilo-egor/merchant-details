package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class MansoryHighCheckPropertiesTest {

    @Autowired
    private MansoryHighCheckProperties mansoryProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("mansory-high-check-url", mansoryProperties.url()),
                () -> assertEquals("mansory-high-check-api-key", mansoryProperties.apiKey()),
                () -> assertEquals("mansory-high-check-secret", mansoryProperties.secret())
        );
    }
}