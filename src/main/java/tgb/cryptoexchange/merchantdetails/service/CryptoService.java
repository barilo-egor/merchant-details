package tgb.cryptoexchange.merchantdetails.service;

import com.macasaet.fernet.Key;
import com.macasaet.fernet.StringValidator;
import com.macasaet.fernet.Token;
import com.macasaet.fernet.Validator;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.temporal.TemporalAmount;

@Service
public class CryptoService {

    @SuppressWarnings("java:S1604")
    public String decrypt(String keyB64, String tokenB64) {
        final Key key = new Key(keyB64);
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
