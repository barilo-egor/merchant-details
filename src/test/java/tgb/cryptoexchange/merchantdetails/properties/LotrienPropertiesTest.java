package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class LotrienPropertiesTest {

    @Autowired
    private LotrienProperties lotrienProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("lotrien-url", lotrienProperties.url()),
                () -> assertEquals("lotrien-key", lotrienProperties.key())
        );
    }
}