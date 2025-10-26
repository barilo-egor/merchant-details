package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PandaPayPropertiesTest {

    @Autowired
    private PandaPayProperties pandaPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("panda-pay-url", pandaPayProperties.url()),
                () -> assertEquals("panda-pay-key", pandaPayProperties.key()),
                () -> assertEquals("panda-pay-secret", pandaPayProperties.secret())
        );
    }
}