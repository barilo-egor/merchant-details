package tgb.cryptoexchange.merchantdetails.details.yolo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.io.IOException;
import java.time.Instant;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JwtData {

    private String accessToken;

    @JsonDeserialize(using = InstantDeserializer.class)
    private Instant expiresAt;

    public static class InstantDeserializer extends JsonDeserializer<Instant> {
        @Override
        public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return Instant.parse(p.getText());
        }
    }

}