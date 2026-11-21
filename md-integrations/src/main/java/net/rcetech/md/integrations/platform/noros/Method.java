package net.rcetech.md.integrations.platform.noros;

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
    SBP(8, "СБП", RequestMethod.SBP),
    CARD(1, "Перевод по номеру карты", RequestMethod.CARD);

    private final Integer value;

    private final String description;

    final RequestMethod requestMethod;

    public Optional<RequestMethod> getRequestMethod() {
        return Optional.ofNullable(requestMethod);
    }

    public static class Serializer extends ValueSerializer<Method> {
        @Override
        public void serialize(Method notosMethod, JsonGenerator jsonGenerator, SerializationContext serializerProvider) {
            jsonGenerator.writeNumber(notosMethod.getValue());
        }
    }
}
