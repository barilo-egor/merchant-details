package tgb.cryptoexchange.merchantdetails.details.tronex;

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

@AllArgsConstructor
@Getter
public enum Method implements MerchantMethod {
    CARD("c2c", null, "Карта", RequestMethod.CARD),
    SBP("sbp", null, "СБП", RequestMethod.SBP),
    SIM("mc", null, "Sim", null),
    SBER_QR("vietqr", "sber", "Sber QR", null),
    ALFA_QR("vietqr", "alfa", "Alfa QR", null),
    VTB_QR("vietqr", "vtb", "VTB QR", null);

    final String methodUid;

    final String sourceBank;

    final String description;

    final RequestMethod requestMethod;

    public Optional<RequestMethod> getRequestMethod() {
        return Optional.ofNullable(requestMethod);
    }

    public static Method fromValue(String v) {
        for (Method method : Method.values()) {
            if (method.methodUid.equals(v)) {
                return method;
            }
        }
        return null;
    }

    public static class Serializer extends JsonSerializer<Method> {
        @Override
        public void serialize(Method method, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(method.getMethodUid());
        }
    }

    public static class Deserializer extends JsonDeserializer<Method> {

        @Override
        public Method deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            return Method.fromValue(jsonParser.getValueAsString());
        }
    }
}
