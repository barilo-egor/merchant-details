package tgb.cryptoexchange.merchantdetails.details.noros;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;

import java.io.IOException;

@AllArgsConstructor
@Getter
public enum Method implements MerchantMethod {
    SBP(8, "СБП"),
    CARD(1, "Перевод по номеру карты");

    private final Integer value;

    private final String description;

    public static class Serializer extends JsonSerializer<Method> {
        @Override
        public void serialize(Method notosMethod, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeNumber(notosMethod.getValue());
        }
    }
}
