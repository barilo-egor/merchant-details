package tgb.cryptoexchange.merchantdetails.details.yolo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;

import java.io.IOException;

@Getter
@AllArgsConstructor
public enum Method implements MerchantMethod {
    SBP("SBP", "СБП"),
    TO_CARD("TO_CARD", "Перевод на карту");

    private final String value;

    private final String description;

    public static class Serializer extends JsonSerializer<Method> {
        @Override
        public void serialize(Method method, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(method.getValue());
        }
    }
}
