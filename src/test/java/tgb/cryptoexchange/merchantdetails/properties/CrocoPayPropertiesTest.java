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
                () -> assertEquals("croco-pay-url", crocoPayProperties.url()),
                () -> assertEquals("croco-pay-client-id", crocoPayProperties.clientId()),
                () -> assertEquals("croco-pay-client-secret", crocoPayProperties.clientSecret())
        );
    }
}