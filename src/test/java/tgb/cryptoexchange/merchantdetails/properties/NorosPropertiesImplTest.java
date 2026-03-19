package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class NorosPropertiesImplTest {

    @Autowired
    private NorosPropertiesImpl norosPropertiesImpl;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("noros-url", norosPropertiesImpl.url()),
                () -> assertEquals("noros-key", norosPropertiesImpl.key())
        );
    }

}