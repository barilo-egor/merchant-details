package tgb.cryptoexchange.merchantdetails.details.viatrum;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;

import java.io.IOException;
import java.util.Arrays;

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

    public static class Serializer extends JsonSerializer<Method> {
        @Override
        public void serialize(Method viatrum, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            if (Arrays.asList(VTB_QR, SBER_QR, ALFA_QR, OTP_QR, GAZPROM_QR, PSB_QR).contains(viatrum)) {
                jsonGenerator.writeString("PAYMENT_LINK");
                return;
            }
            jsonGenerator.writeString(viatrum.getValue());
        }
    }

    public static class Deserializer extends
            JsonDeserializer<Method> {
        @Override
        public Method deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            return fromValue(jsonParser.getValueAsString());
        }
    }
}
