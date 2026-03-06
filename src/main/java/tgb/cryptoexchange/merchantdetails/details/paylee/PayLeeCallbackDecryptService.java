package tgb.cryptoexchange.merchantdetails.details.paylee;

import com.macasaet.fernet.Key;
import com.macasaet.fernet.StringValidator;
import com.macasaet.fernet.Token;
import com.macasaet.fernet.Validator;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.CallbackDecryptService;
import tgb.cryptoexchange.merchantdetails.properties.PayLeePropertiesImpl;

import java.time.Duration;
import java.time.temporal.TemporalAmount;

@Service
public class PayLeeCallbackDecryptService implements CallbackDecryptService {

    private final PayLeePropertiesImpl payLeeProperties;

    public PayLeeCallbackDecryptService(PayLeePropertiesImpl payLeeProperties) {
        this.payLeeProperties = payLeeProperties;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PAY_LEE;
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
