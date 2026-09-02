package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class WatPdfPropertiesTest {

    @Autowired
    private WatPdfProperties watProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("wat-pdf-token", watProperties.token())
        );
    }
}