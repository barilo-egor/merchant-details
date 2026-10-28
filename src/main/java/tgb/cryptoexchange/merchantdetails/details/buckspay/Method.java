package tgb.cryptoexchange.merchantdetails.details.buckspay;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;
import tgb.cryptoexchange.merchantdetails.detailsapi.enums.RequestMethod;

import java.io.IOException;
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

    public static class Serializer extends JsonSerializer<Method> {
        @Override
        public void serialize(Method method, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(method.getValue());
        }
    }

}
