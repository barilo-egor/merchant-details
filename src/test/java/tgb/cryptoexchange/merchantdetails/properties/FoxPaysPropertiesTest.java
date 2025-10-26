package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FoxPaysPropertiesTest {

    @Autowired
    private FoxPaysProperties foxPaysProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("fox-pays-url", foxPaysProperties.url()),
                () -> assertEquals("fox-pays-token", foxPaysProperties.token()),
                () -> assertEquals("fox-pays-merchant-id", foxPaysProperties.merchantId())
        );
    }
}