package tgb.cryptoexchange.merchantdetails.details.fiatcut;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;
import tgb.cryptoexchange.merchantdetails.detailsapi.enums.RequestMethod;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.util.Optional;

@Getter
@AllArgsConstructor
public enum Method implements MerchantMethod {
    CARD("card", "Карта", RequestMethod.CARD),
    PHONE("phone", "СБП", RequestMethod.SBP);

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
