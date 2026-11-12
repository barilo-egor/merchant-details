package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AlfaTeamWTPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private AlfaTeamWTProperties alfaTeamWTProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("alfa-team-white-triangle-url", alfaTeamWTProperties.url()),
                () -> assertEquals("alfa-team-white-triangle-key", alfaTeamWTProperties.key()),
                () -> assertEquals("alfa-team-white-triangle-token", alfaTeamWTProperties.token()),
                () -> assertEquals("alfa-team-white-triangle-secret", alfaTeamWTProperties.secret())
        );
    }
}