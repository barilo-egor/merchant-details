package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CubeLowCheckPropertiesTest extends AbstractMySqlTest {

    @Autowired
    private CubeLowCheckProperties cubeProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("cube-low-check-url", cubeProperties.url()),
                () -> assertEquals("cube-low-check-key", cubeProperties.key()),
                () -> assertEquals("cube-low-check-private-key", cubeProperties.privateKey())
        );
    }
}