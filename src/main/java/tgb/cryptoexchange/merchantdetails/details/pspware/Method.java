package tgb.cryptoexchange.merchantdetails.details.pspware;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;

@AllArgsConstructor
@Getter
public enum Method {
    SBP("sbp", "СБП"),
    CARD("c2c", "Карта"),
    TRANSGRAN_PHONE("transgran_phone", "Трансгран номер телефона");

    private final String value;

    private final String description;

    public static class Serializer extends JsonSerializer<Method> {
        @Override
        public void serialize(Method pspWareMethod, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(pspWareMethod.getValue());
        }
    }
}
