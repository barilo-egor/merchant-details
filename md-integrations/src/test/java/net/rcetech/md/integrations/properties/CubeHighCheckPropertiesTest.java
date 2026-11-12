package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CubeHighCheckPropertiesTest extends AbstractIntegrationsContextTest {

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