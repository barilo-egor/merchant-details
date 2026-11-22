package net.rcetech.md.integrations.platform.paylee;

import com.macasaet.fernet.Key;
import com.macasaet.fernet.StringValidator;
import com.macasaet.fernet.Token;
import com.macasaet.fernet.Validator;
import net.rcetech.md.integrations.platform.CallbackDecryptService;
import net.rcetech.md.integrations.properties.PayLeeProperties;

import java.time.Duration;
import java.time.temporal.TemporalAmount;

public abstract class PayLeeCallbackDecryptService implements CallbackDecryptService {

    private final PayLeeProperties payLeeProperties;

    protected PayLeeCallbackDecryptService(PayLeeProperties payLeeProperties) {
        this.payLeeProperties = payLeeProperties;
    }

    @SuppressWarnings("java:S1604")
    public String decrypt(String tokenB64) {
        final Key key = new Key(payLeeProperties.secret());
        final Token token = Token.fromString(tokenB64);
        final Validator<String> validator = new StringValidator() {
            @Override
            public TemporalAmount getTimeToLive() {
                return Duration.ofHours(24);
            }
        };
        return token.validateAndDecrypt(key, validator);
    }
}
