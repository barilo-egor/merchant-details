package net.rcetech.md.integrations.platform.cashout;

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
    CARD("CARD", "Карта", RequestMethod.CARD),
    SBP("SBP", "СБП", RequestMethod.SBP),
    ;

    private final String value;

    private final String description;

    final RequestMethod requestMethod;

    public Optional<RequestMethod> getRequestMethod() {
        return Optional.ofNullable(requestMethod);
    }

    public static class Serializer extends ValueSerializer<Method> {

        @Override
        public void serialize(Method value, JsonGenerator gen, SerializationContext serializers) {
            gen.writeString(value.getValue());
        }
    }
}
