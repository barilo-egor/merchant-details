package net.rcetech.md.integrations.platform.buckspay;

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
    SBP("SBP", 0, "СБП", RequestMethod.SBP),
    CARD("Card", 0, "Перевод на карту", RequestMethod.CARD),
    T_PAY("SBP", 2, "СБП Т-банк — Т-банк", null),
    NSPK("QRManager", 0, "НСПК", null),
    SIM("MobileCommerce", 0, "Sim", null),
    ;

    private final String value;

    private final Integer bankCode;

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
