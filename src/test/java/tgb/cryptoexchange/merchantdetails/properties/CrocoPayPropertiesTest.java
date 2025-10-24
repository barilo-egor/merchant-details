package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CrocoPayPropertiesTest {

    @Autowired
    private CrocoPayProperties crocoPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("croco-pay-key", crocoPayProperties.url()),
                () -> assertEquals("croco-pay-url", crocoPayProperties.clientId()),
                () -> assertEquals("croco-pay-url", crocoPayProperties.clientSecret())
        );
    }
}