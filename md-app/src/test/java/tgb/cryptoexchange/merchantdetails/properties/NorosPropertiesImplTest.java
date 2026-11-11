package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NorosPropertiesImplTest extends AbstractMySqlTest {

    @Autowired
    private NorosPropertiesImpl norosPropertiesImpl;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("noros-url", norosPropertiesImpl.url()),
                () -> assertEquals("noros-key", norosPropertiesImpl.key()),
                () -> assertEquals("noros-client-id-salt", norosPropertiesImpl.clientIdSalt())
        );
    }

}