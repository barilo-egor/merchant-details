package net.rcetech.md.integrations.platform.cube;


import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantMethod;
import net.rcetech.md.meta.detailsapi.enums.RequestMethod;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.util.Optional;

@Getter
@AllArgsConstructor
public enum Method implements MerchantMethod {
    CARD("INTERBRUB", "Карта", RequestMethod.CARD),
    SBP("SBPRUB", "СБП", RequestMethod.SBP),
    SIM("MOBCOMRUB", "СИМ", null);

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
}

