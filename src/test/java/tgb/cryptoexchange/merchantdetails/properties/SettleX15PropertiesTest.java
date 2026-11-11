package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SettleX15PropertiesTest extends AbstractMySqlTest {

    @Autowired
    private SettleX15Properties settleXProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("settle-x15-url", settleXProperties.url()),
                () -> assertEquals("settle-x15-key", settleXProperties.key()),
                () -> assertEquals("settle-sbp-id", settleXProperties.sbpId()),
                () -> assertEquals("settle-c2c-id", settleXProperties.c2cId())
        );
    }
}