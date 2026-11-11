package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CubePropertiesTest extends AbstractMySqlTest {

    @Autowired
    private CubePropertiesImpl cubeProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("cube-url", cubeProperties.url()),
                () -> assertEquals("cube-key", cubeProperties.key()),
                () -> assertEquals("cube-private-key", cubeProperties.privateKey())
        );
    }
}