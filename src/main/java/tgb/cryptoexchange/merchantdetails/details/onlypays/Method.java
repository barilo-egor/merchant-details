package tgb.cryptoexchange.merchantdetails.details.onlypays;

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
    CARD("card", "Карта"),
    SBP("sbp", "СБП"),
    SIM("sbp", "SIM"),
    ALFA_ALFA("sbp", "Альфа-Альфа"),
    OZON_OZON("sbp", "Озон-Озон");

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
}
