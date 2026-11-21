package net.rcetech.md.integrations.platform.prismapay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantMethod;
import net.rcetech.md.meta.detailsapi.enums.RequestMethod;
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
    SBP("phone", "СБП", RequestMethod.SBP);

    final String value;

    final String description;

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

    public static class Serializer extends ValueSerializer<Method> {
        @Override
        public void serialize(Method method, JsonGenerator jsonGenerator, SerializationContext serializerProvider) {
            jsonGenerator.writeString(method.getValue());
        }
    }

    public static class Deserializer extends ValueDeserializer<Method> {

        @Override
        public Method deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
            return Method.fromValue(jsonParser.getValueAsString());
        }
    }
}