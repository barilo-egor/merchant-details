package tgb.cryptoexchange.merchantdetails.details.pspware;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResponseTest {

    @Test
    void validateShouldReturnNoErrorsIfAllFieldsPresent() {
        Response response = new Response();
        response.setId("id");
        response.setStatus(Status.PROCESSING);
        response.setBankName("bankName");
        response.setCard("card");
        assertTrue(response.validate().errorsToString().isEmpty());
    }

    @Test
    void validateShouldReturnErrorIfIdIsNull() {
        Response response = new Response();
        response.setStatus(Status.PROCESSING);
        response.setBankName("bankName");
        response.setCard("card");
        assertEquals("field \"id\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfStatusIsNull() {
        Response response = new Response();
        response.setId("id");
        response.setBankName("bankName");
        response.setCard("card");
        assertEquals("field \"status\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfBankNameIsNull() {
        Response response = new Response();
        response.setId("id");
        response.setStatus(Status.PROCESSING);
        response.setCard("card");
        assertEquals("field \"bankName\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfCardIsNull() {
        Response response = new Response();
        response.setId("id");
        response.setStatus(Status.PROCESSING);
        response.setBankName("bankName");
        assertEquals("field \"card\" must not be null", response.validate().errorsToString());
    }

    @Test
    void hasDetailsShouldReturnTrue() {
        assertTrue(new Response().hasDetails());
    }
}