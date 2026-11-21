package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PrismaPayPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private PrismaPayProperties prismaPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("prisma-pay-url", prismaPayProperties.url()),
                () -> assertEquals("prisma-pay-token", prismaPayProperties.token())
        );
    }
}