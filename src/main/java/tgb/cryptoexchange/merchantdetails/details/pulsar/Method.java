package tgb.cryptoexchange.merchantdetails.details.pulsar;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;

@AllArgsConstructor
@Getter
public enum Method {
    CARD("c2c", "Карта"),
    SBP("sbp", "СБП");

    private final String value;

    private final String description;

    public static Method fromValue(String value) {
        for (Method m : Method.values()) {
            if (m.getValue().equals(value)) {
                return m;
            }
        }
        return null;
    }

    public static class Serializer extends JsonSerializer<Method> {
        @Override
        public void serialize(Method pulsarMethod, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(pulsarMethod.getValue());
        }
    }
}
