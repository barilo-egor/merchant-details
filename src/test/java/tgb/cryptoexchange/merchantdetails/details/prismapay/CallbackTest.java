package tgb.cryptoexchange.merchantdetails.details.prismapay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class CallbackTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void isIgnoredShouldReturnTrueWhenStatusIsNoRequisites() {
        Callback callback = new Callback();
        callback.setStatus(Status.NO_REQUISITES);

        assertTrue(callback.isIgnored());
    }

    @Test
    void isIgnoredShouldReturnFalseWhenStatusIsNull() {
        Callback callback = new Callback();
        callback.setStatus(null);

        assertFalse(callback.isIgnored());
    }

    @ParameterizedTest
    @EnumSource(value = Status.class, names = "NO_REQUISITES", mode = EnumSource.Mode.EXCLUDE)
    void isIgnoredShouldReturnFalseWhenStatusIsNotNoRequisites(Status status) {
        Callback callback = new Callback();
        callback.setStatus(status);

        assertFalse(callback.isIgnored());
    }

    @Test
    void shouldDeserializeCallbackWithNoRequisitesStatus() throws JsonProcessingException {
        String json = "{\"id\":\"test-id-123\",\"status\":\"NO_REQUISITES\"}";

        Callback callback = objectMapper.readValue(json, Callback.class);

        assertEquals("test-id-123", callback.getId());
        assertEquals(Status.NO_REQUISITES, callback.getStatus());
        assertTrue(callback.isIgnored());
    }

    @Test
    void shouldDeserializeCallbackWithSuccessStatus() throws JsonProcessingException {
        String json = "{\"id\":\"test-id-456\",\"status\":\"SUCCESS\"}";

        Callback callback = objectMapper.readValue(json, Callback.class);

        assertEquals("test-id-456", callback.getId());
        assertEquals(Status.SUCCESS, callback.getStatus());
        assertFalse(callback.isIgnored());
    }
}
