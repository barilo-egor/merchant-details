package tgb.cryptoexchange.merchantdetails.details.lotrien;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;
import tgb.cryptoexchange.merchantdetails.detailsapi.enums.RequestMethod;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@Getter
public enum Method implements MerchantMethod {
    BANK_CARD("Карта", RequestMethod.CARD),
    SBP("СБП", RequestMethod.SBP),
    PDF_BANK_CARD("Карта PDF", null),
    PDF_SBP("СБП PDF", null),
    ;

    final String description;

    final RequestMethod requestMethod;

    public Optional<RequestMethod> getRequestMethod() {
        return Optional.ofNullable(requestMethod);
    }

    public static class Serializer extends JsonSerializer<Method> {
        @Override
        public void serialize(Method method, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeArray(new String[]{method.name()}, 0, 1);
        }
    }
}
