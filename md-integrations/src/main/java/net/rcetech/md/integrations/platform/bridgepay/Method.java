package net.rcetech.md.integrations.platform.bridgepay;

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

@Getter
@AllArgsConstructor
public enum Method implements MerchantMethod {
    SBP("SBP", "СБП", RequestMethod.SBP),
    TO_CARD("TO_CARD", "Перевод на карту", RequestMethod.CARD),
    CROSS_BORDER("CROSS_BORDER", "Трансграничный перевод", null),
    MOBILE_TOP_UP("MOBILE_TOP_UP", "Пополнение счета моб.телефона", null),
    SBP_QR("SBP_QR", "СБП QR", null),
    MANUAL_SBP_QR("MANUAL_SBP_QR", "Ручной QR", null);

    private final String value;

    private final String description;

    final RequestMethod requestMethod;

    public Optional<RequestMethod> getRequestMethod() {
        return Optional.ofNullable(requestMethod);
    }

    public static class Serializer extends ValueSerializer<Method> {
        @Override
        public void serialize(Method method, JsonGenerator jsonGenerator, SerializationContext serializerProvider) {
            jsonGenerator.writeString(method.getValue());
        }
    }

    private static Method fromValue(String value) {
        for (Method method : Method.values()) {
            if (method.getValue().equals(value)) {
                return method;
            }
        }
        return null;
    }

    public static class Deserializer extends ValueDeserializer<Method> {
        @Override
        public Method deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
            return Method.fromValue(jsonParser.getValueAsString());
        }
    }
}
