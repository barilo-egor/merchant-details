package tgb.cryptoexchange.merchantdetails.details.levelpay;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;

@AllArgsConstructor
@Getter
public enum Method {
    CARD("card", "Карта"),
    PHONE("phone", "СБП"),
    ALFA_ALFA("phone", "Альфа-альфа");

    private final String value;

    private final String description;

    public static class Serializer extends JsonSerializer<Method> {

        @Override
        public void serialize(Method value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.getValue());
        }
    }
}
