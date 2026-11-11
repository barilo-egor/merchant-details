package tgb.cryptoexchange.merchantdetails.details.bitzone;

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

@Getter
@AllArgsConstructor
public enum Method implements MerchantMethod {
    SBP("СБП", RequestMethod.SBP),
    CARD("Карта", RequestMethod.CARD);

    private final String description;

    final RequestMethod requestMethod;

    public Optional<RequestMethod> getRequestMethod() {
        return Optional.ofNullable(requestMethod);
    }

    public static class Serializer extends ValueSerializer<Method> {

        @Override
        public void serialize(Method value, JsonGenerator gen, SerializationContext serializers) {
            gen.writeString(value.name().toLowerCase());
        }
    }

    public static class Deserializer extends ValueDeserializer<Method> {
        @Override
        public Method deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
            return Method.valueOf(jsonParser.getString().toUpperCase());
        }
    }
}
