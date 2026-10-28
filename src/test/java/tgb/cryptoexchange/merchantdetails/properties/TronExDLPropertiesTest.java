package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class TronExDLPropertiesTest {

    @Autowired
    private TronExDLProperties tronExProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("tron-ex-dl-key", tronExProperties.key()),
                () -> assertEquals("tron-ex-dl-dl-url", tronExProperties.dlUrl())
        );
    }
}