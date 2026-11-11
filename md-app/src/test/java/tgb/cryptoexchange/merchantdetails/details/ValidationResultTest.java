package tgb.cryptoexchange.merchantdetails.details;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ValidationResultTest {

    @ValueSource(strings = {
            "field", "amount", "status"
    })
    @ParameterizedTest
    void notNullShouldAddError() {
        ValidationResult result = new ValidationResult();
        result.notNull("field1");
        assertEquals("field \"field1\" must not be null", result.errorsToString());
    }

    @Test
    void notNullShouldAddErrors() {
        ValidationResult result = new ValidationResult();
        result.notNull("field1", "field2", "field3");
        assertEquals("field \"field1 or field2 or field3\" must not be null", result.errorsToString());
    }

    @CsvSource(textBlock = """
            errors,5
            invalid,10
            """)
    @ParameterizedTest
    void expectedEmptyShouldAddError(String field, String actual) {
        ValidationResult result = new ValidationResult();
        result.expectedEmpty(field, actual);
        assertEquals(
                "field \"" + field + "\" must be empty but was \"" + actual + "\"",
                result.errorsToString()
        );
    }

    @CsvSource(textBlock = """
            someError,someMessage
            status,expected success but was error
            """)
    @ParameterizedTest
    void addErrorShouldAddError(String field, String message) {
        ValidationResult result = new ValidationResult();
        result.addError(field, message);
        assertEquals("field \"" + field + "\" " + message, result.errorsToString());
    }

    @Test
    void errorsToStringShouldReturnEmptyStringIfNoErrors() {
        ValidationResult result = new ValidationResult();
        assertEquals("", result.errorsToString());
    }

    @Test
    void isValidShouldReturnTrueIfErrorsIsEmpty() {
        ValidationResult result = new ValidationResult();
        assertTrue(result.isValid());
    }

    @Test
    void isValidShouldReturnFalseIfErrorsIsNotEmpty() {
        ValidationResult result = new ValidationResult();
        result.addError("field", "message");
        assertFalse(result.isValid());
    }
}