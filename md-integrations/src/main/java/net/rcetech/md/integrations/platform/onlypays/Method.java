package net.rcetech.md.integrations.platform.onlypays;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantMethod;
import net.rcetech.md.meta.detailsapi.enums.RequestMethod;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.util.Optional;

@AllArgsConstructor
@Getter
public enum Method implements MerchantMethod {
    CARD("card", "Карта", RequestMethod.CARD),
    SBP("sbp", "СБП", RequestMethod.SBP),
    SIM("sbp", "SIM", null),
    ALFA_ALFA("sbp", "Альфа-Альфа", null),
    OZON_OZON("sbp", "Озон-Озон", null);

    private final String value;

    private final String description;

    final RequestMethod requestMethod;

    public Optional<RequestMethod> getRequestMethod() {
        return Optional.ofNullable(requestMethod);
    }

    public static Method fromValue(String value) {
        for (Method method : Method.values()) {
            if (method.getValue().equals(value)) {
                return method;
            }
        }
        return null;
    }

    public static class Serializer extends ValueSerializer<Method> {
        @Override
        public void serialize(Method value, JsonGenerator gen, SerializationContext serializers) {
            gen.writeString(value.getValue());
        }
    }
}
