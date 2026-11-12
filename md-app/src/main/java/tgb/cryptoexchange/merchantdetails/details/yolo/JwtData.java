package tgb.cryptoexchange.merchantdetails.details.yolo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.time.Instant;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JwtData {

    private String accessToken;

    @JsonDeserialize(using = InstantDeserializer.class)
    private Instant expiresAt;

    public static class InstantDeserializer extends ValueDeserializer<Instant> {
        @Override
        public Instant deserialize(JsonParser p, DeserializationContext ctxt) {
            return Instant.parse(p.getString());
        }
    }

}