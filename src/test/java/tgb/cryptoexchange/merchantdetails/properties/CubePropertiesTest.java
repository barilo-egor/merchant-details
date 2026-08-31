package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class CubePropertiesTest {

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