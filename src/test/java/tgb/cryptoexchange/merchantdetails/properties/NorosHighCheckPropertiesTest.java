package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class NorosHighCheckPropertiesTest {

    @Autowired
    private NorosHighCheckProperties norosHighCheckProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("noros-high-check-url", norosHighCheckProperties.url()),
                () -> assertEquals("noros-high-check-key", norosHighCheckProperties.key())
        );
    }

}