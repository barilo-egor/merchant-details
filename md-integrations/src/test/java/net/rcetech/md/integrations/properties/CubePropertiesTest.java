package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CubePropertiesTest extends AbstractIntegrationsContextTest {

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