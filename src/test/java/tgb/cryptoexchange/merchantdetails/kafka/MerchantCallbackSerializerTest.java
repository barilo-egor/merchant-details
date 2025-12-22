package tgb.cryptoexchange.merchantdetails.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MerchantCallbackSerializerTest {

    @Test
    void serializeShouldReturnEmptyArrayIfEventIsNull() {
        try (var serializer = new MerchantCallbackSerializer()) {
            assertEquals(0, serializer.serialize("", null).length);
        }
    }

    @CsvSource(delimiter = ';', textBlock = """
            {"merchantOrderId":"99f0213a-3828-4dc9-8417-2e22fa140f13","status":"COMPLETED","statusDescription":"Завершен","merchant":"ALFA_TEAM"};\
            99f0213a-3828-4dc9-8417-2e22fa140f13;COMPLETED;Завершен;ALFA_TEAM
            {"merchantOrderId":"a0af4b95-c0be-426d-8a26-94e13562527f","status":"ERROR","statusDescription":"Ошибка","merchant":"WELL_BIT"};\
            a0af4b95-c0be-426d-8a26-94e13562527f;ERROR;Ошибка;WELL_BIT
            """)
    @ParameterizedTest
    void serializerShouldSerialize(String expected, String orderId, String status, String statusDescription, Merchant merchant) {
        var event = new MerchantCallbackEvent();
        event.setMerchant(merchant);
        event.setStatus(status);
        event.setStatusDescription(statusDescription);
        event.setMerchantOrderId(orderId);
        try (var serializer = new MerchantCallbackSerializer()) {
            assertEquals(expected, new String(serializer.serialize("topic", event), StandardCharsets.UTF_8));
        }
    }
}