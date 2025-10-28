package tgb.cryptoexchange.merchantdetails.details.whitelabel;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;

@Getter
@AllArgsConstructor
public enum PaymentOption {
    SBP("SBP", "СБП"),
    TO_CARD("TO_CARD", "Перевод на карту"),
    CROSS_BORDER("CROSS_BORDER", "Трансграничный перевод");

    private final String value;

    private final String description;

    public static class Serializer extends JsonSerializer<PaymentOption> {
        @Override
        public void serialize(PaymentOption paymentOption, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(paymentOption.getValue());
        }
    }
}
