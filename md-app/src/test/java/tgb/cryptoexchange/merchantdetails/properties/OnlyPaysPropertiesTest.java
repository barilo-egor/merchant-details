package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OnlyPaysPropertiesTest extends AbstractMySqlTest {

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