package tgb.cryptoexchange.merchantdetails.details.wat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;
import tgb.cryptoexchange.merchantdetails.detailsapi.enums.RequestMethod;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;

import java.util.Optional;

@AllArgsConstructor
@Getter
public enum Method implements MerchantMethod {
    CARD("card", "Карта", RequestMethod.CARD),
    SBP("sbp", "СБП", RequestMethod.SBP),
    SIM("sim", "Sim", null),
    BT_PDF("bt-pdf", "BT PDF карта", null),
    BT_PDF_SBP("bt-pdf-sbp", "BT PDF СБП", null);

    final String methodUid;

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

    public static class Serializer extends ValueSerializer<Method> {
        @Override
        public void serialize(Method method, JsonGenerator jsonGenerator, SerializationContext serializerProvider) {
            jsonGenerator.writeString(method.getMethodUid());
        }
    }

    public static class Deserializer extends ValueDeserializer<Method> {

        @Override
        public Method deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
            return Method.fromValue(jsonParser.getValueAsString());
        }
    }
}
