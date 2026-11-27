package net.rcetech.md.integrations.platform.paylee;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantMethod;
import net.rcetech.md.meta.detailsapi.enums.RequestMethod;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

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

    @JsonValue
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

    /**
     * Кастомный десериализатор сохранён: принимает и массив, и одиночную строку.
     */
    public static class Deserializer extends ValueDeserializer<List<Method>> {

        @Override
        public List<Method> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
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
