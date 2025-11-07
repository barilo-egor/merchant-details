package tgb.cryptoexchange.merchantdetails.details.pulsar;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {

    @Test
    void validateShouldReturnErrorIfStatusIsFalse() {
        Response response = new Response();
        response.setStatus(false);
        assertEquals("field \"success\" expected true but was false", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfResultIsNull() {
        Response response = new Response();
        response.setStatus(true);
        assertEquals("field \"result\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfResultIdIsNull() {
        Response response = new Response();
        response.setStatus(true);
        Response.Result result = new Response.Result();
        result.setState(Status.PENDING);
        result.setAddress("address");
        result.setBankName("bankName");
        response.setResult(result);
        assertEquals("field \"result.id\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfResultStateIsNull() {
        Response response = new Response();
        response.setStatus(true);
        Response.Result result = new Response.Result();
        result.setId("id");
        result.setAddress("address");
        result.setBankName("bankName");
        response.setResult(result);
        assertEquals("field \"result.state\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfResultBankNameIsNull() {
        Response response = new Response();
        response.setStatus(true);
        Response.Result result = new Response.Result();
        result.setState(Status.PENDING);
        result.setId("id");
        result.setAddress("address");
        response.setResult(result);
        assertEquals("field \"result.bankName\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfResultAddressIsNull() {
        Response response = new Response();
        response.setStatus(true);
        Response.Result result = new Response.Result();
        result.setId("id");
        result.setState(Status.PENDING);
        result.setBankName("bankName");
        response.setResult(result);
        assertEquals("field \"result.address\" must not be null", response.validate().errorsToString());
    }

    @Test
    void hasDetailsShouldReturnFalseIfStatusIsFalse() {
        Response response = new Response();
        assertFalse(response.hasDetails());
    }

    @Test
    void hasDetailsShouldReturnFalseIfResultIsNull() {
        Response response = new Response();
        response.setStatus(true);
        assertFalse(response.hasDetails());
    }

    @Test
    void hasDetailsShouldReturnTrue() {
        Response response = new Response();
        response.setStatus(true);
        response.setResult(new Response.Result());
        assertTrue(response.hasDetails());
    }
}