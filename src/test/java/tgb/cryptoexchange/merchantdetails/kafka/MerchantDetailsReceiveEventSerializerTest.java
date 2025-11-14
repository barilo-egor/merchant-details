package tgb.cryptoexchange.merchantdetails.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MerchantDetailsReceiveEventSerializerTest {

    @Test
    void serializeShouldReturnEmptyArrayIfEventIsNull() {
        try (var serializer = new MerchantDetailsReceiveEventSerializer()) {
            assertEquals(0, serializer.serialize("", null).length);
        }
    }

    @CsvSource("""
            {"requestedAmount":1250},1250
            {"requestedAmount":3500},3500
            """)
    @ParameterizedTest
    void serializerShouldSerialize(String expected, Integer amount) {
        var event = new MerchantDetailsReceiveEvent();
        event.setRequestedAmount(amount);
        try (var serializer = new MerchantDetailsReceiveEventSerializer()) {
            assertEquals(expected, new String(serializer.serialize("topic", event), StandardCharsets.UTF_8));
        }
    }
}