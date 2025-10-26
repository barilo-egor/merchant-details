package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OnlyPaysPropertiesTest {

    @Autowired
    private OnlyPaysProperties onlyPaysProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("only-pays-url", onlyPaysProperties.url()),
                () -> assertEquals("only-pays-id", onlyPaysProperties.id()),
                () -> assertEquals("only-pays-secret", onlyPaysProperties.secret())
        );
    }
}