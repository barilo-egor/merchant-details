package tgb.cryptoexchange.merchantdetails.details.nicepay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ResponseTest {

    @Test
    void validateShouldReturnErrorIsStatusIsNull() {
        Response response = new Response();
        assertEquals("field \"status\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfStatusIsError() {
        Response response = new Response();
        response.setStatus(Response.ResponseStatus.ERROR);
        assertEquals("field \"status\" must not be ERROR", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnEmptyErrorsIfStatusIsDetailsNotFound() {
        Response response = new Response();
        response.setStatus(Response.ResponseStatus.DETAILS_NOT_FOUND);
        assertTrue(response.validate().errorsToString().isEmpty());
    }

    @Test
    void validateShouldReturnErrorIfDataIsNull() {
        Response response = new Response();
        response.setStatus(Response.ResponseStatus.DETAILS_FOUND);
        assertEquals("field \"data\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfDataPaymentIdIsNull() {
        Response response = new Response();
        response.setStatus(Response.ResponseStatus.DETAILS_FOUND);
        Response.Data data = new Response.Data();
        response.setData(data);
        Response.Data.Details details = new Response.Data.Details();
        details.setWallet("wallet");
        data.setDetails(details);
        assertEquals("field \"data.paymentId\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfDataDetailsIsNull() {
        Response response = new Response();
        response.setStatus(Response.ResponseStatus.DETAILS_FOUND);
        Response.Data data = new Response.Data();
        data.setPaymentId("paymentId");
        response.setData(data);
        assertEquals("field \"data.details\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfDataDetailsWalletIsNull() {
        Response response = new Response();
        response.setStatus(Response.ResponseStatus.DETAILS_FOUND);
        Response.Data data = new Response.Data();
        response.setData(data);
        Response.Data.Details details = new Response.Data.Details();
        data.setPaymentId("paymentId");
        data.setDetails(details);
        assertEquals("field \"data.details.wallet\" must not be null", response.validate().errorsToString());
    }

    @Test
    void hasDetailsShouldReturnFalseIfStatusNotDetailsFound() {
        Response response = new Response();
        response.setStatus(Response.ResponseStatus.ERROR);
        assertFalse(response.hasDetails());
        response.setStatus(Response.ResponseStatus.DETAILS_NOT_FOUND);
        assertFalse(response.hasDetails());
    }
}