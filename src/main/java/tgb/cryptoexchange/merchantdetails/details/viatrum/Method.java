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

@AllArgsConstructor
@Getter
public enum Method implements MerchantMethod {
    CARD("CARD", "Карта", 1),
    SBP("SBP", "СБП", 2),
    VTB_QR("VTB2VTB", "ВТБ QR", 5),
    SBER_QR("SBER2SBER", "Сбер QR", 3),
    ALFA_QR("ALFA2ALFA", "Альфа QR", 6),
    OTP_QR("OTP2OTP", "ОТП QR", 13),
    GAZPROM_QR("GAZPROM2GAZPROM", "Газпром Банк QR", 12);

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


/*
{
    "success": true,
    "data": [
        {
            "id": 1,
            "name": "МежБанк",
            "key": "CARD",
            "currency": "RUB"
        },
        {
            "id": 2,
            "name": "ПСБ",
            "key": "PSB",
            "currency": "RUB"
        },
        {
            "id": 3,
            "name": "Сбербанк",
            "key": "SBER",
            "currency": "RUB"
        },
        {
            "id": 4,
            "name": "ТБанк",
            "key": "TBANK",
            "currency": "RUB"
        },
        {
            "id": 5,
            "name": "ВТБ",
            "key": "VTB",
            "currency": "RUB"
        },
        {
            "id": 6,
            "name": "Альфа Банк",
            "key": "ALFA",
            "currency": "RUB"
        },
        {
            "id": 7,
            "name": "Озон Банк (Ozon)",
            "key": "OZON",
            "currency": "RUB"
        },
        {
            "id": 8,
            "name": "Юмани",
            "key": "YOOMONEY",
            "currency": "RUB"
        },
        {
            "id": 9,
            "name": "Вайлдберриз",
            "key": "WILDBERRIES",
            "currency": "RUB"
        },
        {
            "id": 10,
            "name": "Екатеринбург",
            "key": "EKATERINBURG",
            "currency": "RUB"
        },
        {
            "id": 11,
            "name": "Кредит Европа Банк",
            "key": "EUROPE",
            "currency": "RUB"
        },
        {
            "id": 12,
            "name": "Газпром Банк",
            "key": "GAZPROM",
            "currency": "RUB"
        },
        {
            "id": 13,
            "name": "ОТП",
            "key": "OTP",
            "currency": "RUB"
        },
        {
            "id": 14,
            "name": "Уралсиб Банк",
            "key": "URALSIB",
            "currency": "RUB"
        },
        {
            "id": 15,
            "name": "Райффайзен",
            "key": "RAIFFEISEN",
            "currency": "RUB"
        }
    ]
}
 */