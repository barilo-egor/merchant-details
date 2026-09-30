package tgb.cryptoexchange.merchantdetails.details.rspay;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;
import tgb.cryptoexchange.merchantdetails.detailsapi.enums.RequestMethod;

import java.io.IOException;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum Method implements MerchantMethod {
    SBP("sbp", "СБП", RequestMethod.SBP),
    CARD("card", "Перевод на карту", RequestMethod.CARD),
    QR("sberbank_qr_vnm", "QR", null),
    SIM("mobile_number ", "Sim", null);

    private final String value;

    private final String description;

    final RequestMethod requestMethod;

    public Optional<RequestMethod> getRequestMethod() {
        return Optional.ofNullable(requestMethod);
    }

    public static Method fromValue(String v) {
        for (Method method : Method.values()) {
            if (method.value.equals(v)) {
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
