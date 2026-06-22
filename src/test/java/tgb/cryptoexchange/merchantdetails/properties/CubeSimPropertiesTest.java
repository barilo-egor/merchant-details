package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class CubeSimPropertiesTest {

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