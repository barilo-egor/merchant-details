package tgb.cryptoexchange.merchantdetails.details.wellbit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {

    @Test
    void validateShouldReturnNoErrorsIfPaymentIsNull() {
        Response response = new Response();
        assertTrue(response.validate().errorsToString().isEmpty());
    }


    @Test
    void validateShouldReturnErrorIfPaymentIdIsEmpty() {
        Response response = new Response();
        Response.Payment payment = new Response.Payment();
        response.setPayment(payment);
        payment.setCredential("credential");
        payment.setCredentialAdditionalBank("credentialAdditionalBank");
        payment.setStatus(Status.NEW);
        assertEquals("field \"payment.id\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfPaymentCredentialIsEmpty() {
        Response response = new Response();
        Response.Payment payment = new Response.Payment();
        response.setPayment(payment);
        payment.setId(1L);
        payment.setCredentialAdditionalBank("credentialAdditionalBank");
        payment.setStatus(Status.NEW);
        assertEquals("field \"payment.credential\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfPaymentCredentialAdditionalBankIsEmpty() {
        Response response = new Response();
        Response.Payment payment = new Response.Payment();
        response.setPayment(payment);
        payment.setId(1L);
        payment.setCredential("credential");
        payment.setStatus(Status.NEW);
        assertEquals("field \"payment.credentialAdditionalBank\" must not be null", response.validate().errorsToString());
    }


    @Test
    void validateShouldReturnErrorIfPaymentStatusIsEmpty() {
        Response response = new Response();
        Response.Payment payment = new Response.Payment();
        response.setPayment(payment);
        payment.setId(1L);
        payment.setCredential("credential");
        payment.setCredentialAdditionalBank("credentialAdditionalBank");
        assertEquals("field \"payment.status\" must not be null", response.validate().errorsToString());
    }

    @Test
    void hasDetailsShouldReturnTrueIfPaymentNotNull() {
        Response response = new Response();
        response.setPayment(new Response.Payment());
        assertTrue(response.hasDetails());
    }

    @Test
    void hasDetailsShouldReturnFalseIfPaymentNull() {
        Response response = new Response();
        assertFalse(response.hasDetails());
    }
}