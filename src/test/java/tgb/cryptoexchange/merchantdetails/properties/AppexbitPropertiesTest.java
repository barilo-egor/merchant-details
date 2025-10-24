package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AppexbitPropertiesTest {

    @Autowired
    private AppexbitProperties appexbitProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
            () -> assertEquals("appexbit-url", appexbitProperties.url()),
            () -> assertEquals("appexbit-key", appexbitProperties.key())
        );
    }
}