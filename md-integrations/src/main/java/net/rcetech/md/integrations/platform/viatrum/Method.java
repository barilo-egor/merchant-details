package net.rcetech.md.integrations.platform.viatrum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantMethod;
import net.rcetech.md.meta.detailsapi.enums.RequestMethod;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;

import java.util.Arrays;
import java.util.Optional;

@AllArgsConstructor
@Getter
public enum Method implements MerchantMethod {
    VTB_QR("VTB_QR", "ВТБ QR", 5),
    SBER_QR("SBER_QR", "Сбер QR", 3),
    ALFA_QR("ALFA_QR", "Альфа QR", 6),
    OTP_QR("OTP_QR", "ОТП QR", 13),
    GAZPROM_QR("GAZPROM_QR", "Газпром Банк QR", 12),
    PSB_QR("PSB_QR", "ПСБ QR", 2);

    private final String value;

    private final String description;

    private final Integer id;

    public static Method fromValue(String value) {
        for (Method method : Method.values()) {
            if (method.getValue().equals(value)) {
                return method;
            }
        }
        return null;
    }

    @Override
    public Optional<RequestMethod> getRequestMethod() {
        return Optional.empty();
    }

    public static class Serializer extends ValueSerializer<Method> {
        @Override
        public void serialize(Method viatrum, JsonGenerator jsonGenerator, SerializationContext serializerProvider) {
            if (Arrays.asList(VTB_QR, SBER_QR, ALFA_QR, OTP_QR, GAZPROM_QR, PSB_QR).contains(viatrum)) {
                jsonGenerator.writeString("PAYMENT_LINK");
                return;
            }
            jsonGenerator.writeString(viatrum.getValue());
        }
    }

    public static class Deserializer extends
            ValueDeserializer<Method> {
        @Override
        public Method deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
            return fromValue(jsonParser.getValueAsString());
        }
    }
}
