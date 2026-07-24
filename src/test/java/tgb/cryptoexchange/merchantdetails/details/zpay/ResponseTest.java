package tgb.cryptoexchange.merchantdetails.details.zpay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ResponseTest {

    @Test
    void validateShouldReturnNoErrorsIfAllFieldsPresent() {
        Response response = new Response();
        response.setId(123);
        response.setBankName("bankName");
        response.setNumber("number");
        assertTrue(response.validate().errorsToString().isEmpty());
    }

    @Test
    void validateShouldReturnErrorIfIdIsNull() {
        Response response = new Response();
        response.setBankName("bankName");
        response.setNumber("number");
        assertEquals("field \"id\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfBankNameIsNull() {
        Response response = new Response();
        response.setId(123);
        response.setNumber("number");
        assertEquals("field \"bankName\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfNumberIsNull() {
        Response response = new Response();
        response.setId(123);
        response.setBankName("bankName");
        assertEquals("field \"number\" must not be null", response.validate().errorsToString());
    }

}