package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AlfaTeamPropertiesTest {

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