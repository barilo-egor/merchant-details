package tgb.cryptoexchange.merchantdetails.details.paylee;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;
import tgb.cryptoexchange.merchantdetails.detailsapi.enums.RequestMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Getter
public enum Method implements MerchantMethod {
    CARD("card", "Карта", RequestMethod.CARD),
    SBP("sbp", "СБП", RequestMethod.SBP),
    ANY_QR("any-qr", "QR", null),
    NSPK("nspk", "НСПК", null),
    SBER_QR("sber-qr", "Сбербанк QR", null),
    OZON_QR("ozon-qr", " Ozon Банк QR", null),
    ALFA_QR("alfa-qr", " Альфа-банк QR", null),
    GAZPROM_QR("gazprom-qr", "Газпромбанк QR", null),
    PSBPAY("psbpay", "ПСБ (форма)", null),
    YANDEX_TIPS("tips", "Яндекс чаевые", null),
    ;

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

    public static class Serializer extends JsonSerializer<Method> {
        @Override
        public void serialize(Method payLeeMethod, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(payLeeMethod.getValue());
        }
    }

    public static class Deserializer extends JsonDeserializer<List<Method>> {

        @Override
        public List<Method> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            List<Method> methods = new ArrayList<>();
            if (jsonParser.currentToken() == JsonToken.START_ARRAY) {
                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                    String value = jsonParser.getValueAsString();
                    methods.add(Method.fromValue(value));
                }
            } else {
                String value = jsonParser.getValueAsString();
                methods.add(Method.fromValue(value));
            }
            return methods;
        }
    }
}
