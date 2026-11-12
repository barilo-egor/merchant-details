package tgb.cryptoexchange.merchantdetails.kafka;

import org.junit.jupiter.api.Test;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DetailsResponseSerializerTest {

    @Test
    void serializeShouldReturnEmptyArrayIfResponseIsNull() {
        try (var serializer = new DetailsResponseSerializer()) {
            assertEquals(0, serializer.serialize("topic", null).length);
        }
    }

    @Test
    void serializeShouldWriteBankAndDetailsAsOneField() {
        DetailsResponse response = new DetailsResponse();
        response.setMerchant(Merchant.ALFA_TEAM);
        response.setBank("Сбербанк");
        response.setDetails("2200 0000 0000 0000");

        try (var serializer = new DetailsResponseSerializer()) {
            String json = new String(serializer.serialize("topic", response), StandardCharsets.UTF_8);

            assertTrue(json.contains("\"details\":\"Сбербанк 2200 0000 0000 0000\""), json);
            assertTrue(json.contains("\"merchant\":\"ALFA_TEAM\""), json);
        }
    }
}
