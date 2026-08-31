package tgb.cryptoexchange.merchantdetails.details.buckspay;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;

import java.io.IOException;

@Getter
@AllArgsConstructor
public enum Method implements MerchantMethod {
    SBP("SBP", 0, "СБП"),
    CARD("Card", 0, "Перевод на карту"),
    T_PAY("SBP", 2, "СБП Т-банк — Т-банк"),
    NSPK("QRManager", 0, "НСПК"),
    SIM("MobileCommerce", 0, "Sim");

    private final String value;

    private final Integer bankCode;

    private final String description;

    public static class Serializer extends JsonSerializer<Method> {
        @Override
        public void serialize(Method method, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(method.getValue());
        }
    }

}
