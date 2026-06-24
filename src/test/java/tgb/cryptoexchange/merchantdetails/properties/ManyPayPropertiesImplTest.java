package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ManyPayPropertiesImplTest {

    @Autowired
    private ManyPayPropertiesImpl manyPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("many-pay-url", manyPayProperties.url()),
                () -> assertEquals("many-pay-token", manyPayProperties.token())
        );
    }
}