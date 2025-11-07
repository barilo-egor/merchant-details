package tgb.cryptoexchange.merchantdetails.details.paylee;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResponseTest {

    @Test
    void validateShouldReturnErrorIfIdNull() {
        Response response = new Response();
        response.setRequisites("requisites");
        response.setBankName("bankName");
        response.setStatus(Status.PENDING);
        assertEquals("field \"id\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfRequisitesIsNull() {
        Response response = new Response();
        response.setId(1);
        response.setBankName("bankName");
        response.setStatus(Status.PENDING);
        assertEquals("field \"requisites\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfBankNameIsNull() {
        Response response = new Response();
        response.setId(1);
        response.setRequisites("requisites");
        response.setStatus(Status.PENDING);
        assertEquals("field \"bankName\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfStatusNull() {
        Response response = new Response();
        response.setId(1);
        response.setRequisites("requisites");
        response.setBankName("bankName");
        assertEquals("field \"status\" must not be null", response.validate().errorsToString());
    }

    @Test
    void hasDetailsShouldReturnTrue() {
        assertTrue(new Response().hasDetails());
    }
}