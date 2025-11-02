package tgb.cryptoexchange.merchantdetails.details.nicepay;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;

@AllArgsConstructor
@Getter
@SuppressWarnings("java:S1192")
public enum Method {
    SBP("sbp_rub", "СБП"),
    CARD("any_bank_card", "Карта"),
    SBP_RU("sbp_rub", "СБП РФ"),
    SBP_TRANSGRAN("sbp_rub", "СБП Трансгран"),
    SBP_QR_LINK("sbp_qrlink_rub", "QR")
    ;

    private final String value;

    private final String description;

    public static class Serializer extends JsonSerializer<Method> {

        @Override
        public void serialize(Method value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.getValue());
        }
    }
}
