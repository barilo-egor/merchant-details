package tgb.cryptoexchange.merchantdetails.details.payscrow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResponseTest {

    @Test
    void validateShouldReturnErrorIfIdIsNull() {
        Response response = new Response();
        response.setStatus(Status.UNPAID);
        response.setMethodName("methodName");
        response.setHolderAccount("holderAccount");
        assertEquals("field \"id\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfStatusIsNull() {
        Response response = new Response();
        response.setId("id");
        response.setMethodName("methodName");
        response.setHolderAccount("holderAccount");
        assertEquals("field \"status\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfMethodNameIsNull() {
        Response response = new Response();
        response.setId("id");
        response.setStatus(Status.UNPAID);
        response.setHolderAccount("holderAccount");
        assertEquals("field \"methodName\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfHolderAccountIsNull() {
        Response response = new Response();
        response.setId("id");
        response.setStatus(Status.UNPAID);
        response.setMethodName("methodName");
        assertEquals("field \"holderAccount\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnNoErrorsIfAllFieldsPresent() {
        Response response = new Response();
        response.setId("id");
        response.setStatus(Status.UNPAID);
        response.setHolderAccount("holderAccount");
        response.setMethodName("methodName");
        assertTrue(response.validate().errorsToString().isEmpty());
    }

    @Test
    void hasDetailsShouldReturnTrue() {
        assertTrue(new Response().hasDetails());
    }
}