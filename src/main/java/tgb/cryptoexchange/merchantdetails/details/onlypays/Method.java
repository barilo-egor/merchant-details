package tgb.cryptoexchange.merchantdetails.details.onlypays;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;

@AllArgsConstructor
@Getter
public enum Method {
    CARD("card", "Карта"),
    SBP("sbp", "СБП");

    private final String value;

    private final String description;

    public static Method fromValue(String value) {
        for (Method method : Method.values()) {
            if (method.getValue().equals(value)) {
                return method;
            }
        }
        return null;
    }

    public static class Serializer extends JsonSerializer<Method> {
        @Override
        public void serialize(Method value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.getValue());
        }
    }

    public static class Deserializer extends JsonDeserializer<Method> {

        @Override
        public Method deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return Method.fromValue(p.getValueAsString());
        }
    }
}
