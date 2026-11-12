package net.rcetech.md.integrations.util;

import org.hashids.Hashids;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ClientIdUtilsTest {

    private final Hashids hashids = new Hashids("salt", 8);

    @Test
    void encodeShouldKeepNumericEncoding() {
        assertEquals(hashids.encode(1231231231L), ClientIdUtils.encode(hashids, "1231231231"));
    }

    @ValueSource(strings = {"demo-user-1", "13afb6d4-6e61-4555-a0ae-072c6407ddb5", "пользователь", "12345678901234567890"})
    @ParameterizedTest
    void encodeShouldHashAnyString(String userId) {
        String actual = ClientIdUtils.encode(hashids, userId);
        assertAll(
                () -> assertFalse(actual.isEmpty()),
                () -> assertFalse(actual.contains(userId)),
                () -> assertEquals(actual, ClientIdUtils.encode(hashids, userId))
        );
    }

    @Test
    void encodeShouldDifferForDifferentStrings() {
        assertNotEquals(ClientIdUtils.encode(hashids, "user-1"), ClientIdUtils.encode(hashids, "user-2"));
    }
}
