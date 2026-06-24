package tgb.cryptoexchange.merchantdetails.details.manypay;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;

import java.io.IOException;

@Getter
@AllArgsConstructor
public enum Method implements MerchantMethod {
    SBP("sbp", "СБП"),
    CARD("card", "Перевод на карту");

    private final String value;

    private final String description;

    private static Method fromValue(String value) {
        for (Method method : Method.values()) {
            if (method.getValue().equals(value)) {
                return method;
            }
        }
        return null;
    }

    public static class Serializer extends JsonSerializer<Method> {
        @Override
        public void serialize(Method method, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(method.getValue());
        }
    }

    public static class Deserializer extends JsonDeserializer<Method> {
        @Override
        public Method deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            return Method.fromValue(jsonParser.getValueAsString());
        }
    }
}