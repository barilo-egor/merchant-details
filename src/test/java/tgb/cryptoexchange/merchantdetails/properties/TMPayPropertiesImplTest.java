package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class TMPayPropertiesImplTest {

    @Autowired
    private TMPayProperties tmPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("tm-pay-url", tmPayProperties.url()),
                () -> assertEquals("tm-pay-key", tmPayProperties.key())
        );
    }
}