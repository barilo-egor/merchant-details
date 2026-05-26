package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class MansoryImplPropertiesTest {

    @Autowired
    private MansoryImplProperties mansoryProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("mansory-url", mansoryProperties.url()),
                () -> assertEquals("mansory-api-key", mansoryProperties.apiKey()),
                () -> assertEquals("mansory-secret", mansoryProperties.secret())
        );
    }
}