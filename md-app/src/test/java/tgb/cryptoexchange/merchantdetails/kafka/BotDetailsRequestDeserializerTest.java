package tgb.cryptoexchange.merchantdetails.kafka;

import org.junit.jupiter.api.Test;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.BotDetailsRequest;
import tgb.cryptoexchange.merchantdetails.exception.DeserializeEventException;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BotDetailsRequestDeserializerTest {

    @Test
    void deserializeShouldReturnNullIfDataIsNull() {
        try (var deserializer = new BotDetailsRequestDeserializer()) {
            assertNull(deserializer.deserialize("topic", null));
        }
    }

    @Test
    void deserializeShouldDropMethodsOfUnknownMerchants() {
        String json = "{\"id\":1,\"amount\":1000,\"methods\":["
                + "{\"merchant\":\"ALFA_TEAM\",\"methods\":[\"TO_CARD\"]},"
                + "{\"merchant\":\"NOT_EXISTING_MERCHANT\",\"methods\":[\"SBP\"]}]}";

        try (var deserializer = new BotDetailsRequestDeserializer()) {
            BotDetailsRequest request = deserializer.deserialize("topic", json.getBytes(StandardCharsets.UTF_8));

            assertEquals(1000, request.getAmount());
            assertEquals(1, request.getMethods().size());
            assertEquals(Merchant.ALFA_TEAM, request.getMethods().getFirst().getMerchant());
            assertEquals(List.of("TO_CARD"), request.getMethods().getFirst().getMethods());
        }
    }

    @Test
    void deserializeShouldThrowIfDataIsNotJson() {
        try (var deserializer = new BotDetailsRequestDeserializer()) {
            assertThrows(DeserializeEventException.class,
                    () -> deserializer.deserialize("topic", "not json".getBytes(StandardCharsets.UTF_8)));
        }
    }
}
