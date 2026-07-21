package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class CubeHighCheckPropertiesTest {

    @Autowired
    private CubeHighCheckProperties cubeProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("cube-high-check-url", cubeProperties.url()),
                () -> assertEquals("cube-high-check-key", cubeProperties.key()),
                () -> assertEquals("cube-high-check-private-key", cubeProperties.privateKey())
        );
    }
}