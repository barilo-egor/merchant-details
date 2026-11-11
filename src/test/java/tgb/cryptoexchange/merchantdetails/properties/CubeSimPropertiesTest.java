package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CubeSimPropertiesTest extends AbstractMySqlTest {

    @Autowired
    private CubeSimProperties cubeProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("cube-sim-url", cubeProperties.url()),
                () -> assertEquals("cube-sim-key", cubeProperties.key()),
                () -> assertEquals("cube-sim-private-key", cubeProperties.privateKey())
        );
    }
}