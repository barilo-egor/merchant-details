package tgb.cryptoexchange.merchantdetails.details.payscrow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CallbackTest {

    @Test
    void getMerchantOrderIdShouldReturnEmptyIfPayloadIsNull() {
        assertTrue(new Callback().getMerchantOrderId().isEmpty());
    }

    @Test
    void getStatusShouldReturnEmptyIfPayloadIsNull() {
        assertTrue(new Callback().getStatus().isEmpty());
    }

    @Test
    void getStatusDescriptionShouldReturnEmptyIfPayloadIsNull() {
        assertTrue(new Callback().getStatusDescription().isEmpty());
    }

    @Test
    void getMerchantOrderIdShouldReturnEmptyIfIdIsNull() {
        Callback callback = new Callback();
        callback.setPayload(new Callback.Payload());
        assertTrue(callback.getMerchantOrderId().isEmpty());
    }

    @Test
    void getStatusShouldReturnEmptyIfStatusIsNull() {
        Callback callback = new Callback();
        callback.setPayload(new Callback.Payload());
        assertTrue(callback.getStatus().isEmpty());
    }

    @Test
    void getStatusDescriptionShouldReturnEmptyIfStatusDescriptionIsNull() {
        Callback callback = new Callback();
        callback.setPayload(new Callback.Payload());
        assertTrue(callback.getStatusDescription().isEmpty());
    }

    @ValueSource(strings = {
            "a0af4b95-c0be-426d-8a26-94e13562527f", "f820e164-97f9-4098-9b4d-74707bcbee1b"
    })
    @ParameterizedTest
    void getMerchantOrderIdShouldReturnNotEmptyIfIdIsNotNull(String id) {
        Callback callback = new Callback();
        Callback.Payload payload = new Callback.Payload();
        payload.setId(id);
        callback.setPayload(payload);
        assertTrue(callback.getMerchantOrderId().isPresent());
        assertEquals(id, callback.getMerchantOrderId().get());
    }

    @ValueSource(strings = {
            "COMPLETED", "UNPAID"
    })
    @ParameterizedTest
    void getStatusShouldReturnNotEmptyIfStatusIsNotNull(Status status) {
        Callback callback = new Callback();
        Callback.Payload payload = new Callback.Payload();
        payload.setStatus(status);
        callback.setPayload(payload);
        assertTrue(callback.getStatus().isPresent());
        assertEquals(status.name(), callback.getStatus().get());
    }

    @ValueSource(strings = {
            "COMPLETED", "UNPAID"
    })
    @ParameterizedTest
    void getStatusDescriptionShouldReturnNotEmptyIfStatusDescriptionIsNotNull(Status status) {
        Callback callback = new Callback();
        Callback.Payload payload = new Callback.Payload();
        payload.setStatus(status);
        callback.setPayload(payload);
        assertTrue(callback.getStatusDescription().isPresent());
        assertEquals(status.getDescription(), callback.getStatusDescription().get());
    }
}