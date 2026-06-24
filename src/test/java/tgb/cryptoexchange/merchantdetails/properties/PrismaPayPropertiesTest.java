package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PrismaPayPropertiesTest {

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