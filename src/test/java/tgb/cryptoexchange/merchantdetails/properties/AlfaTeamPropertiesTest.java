package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AlfaTeamPropertiesTest extends AbstractMySqlTest {

    @Autowired
    private AlfaTeamProperties alfaTeamProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("alfa-team-url", alfaTeamProperties.url()),
                () -> assertEquals("alfa-team-key", alfaTeamProperties.key()),
                () -> assertEquals("alfa-team-token", alfaTeamProperties.token()),
                () -> assertEquals("alfa-team-secret", alfaTeamProperties.secret())
        );
    }
}