package tgb.cryptoexchange.merchantdetails.details.crocopay;

import org.junit.jupiter.api.Test;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {

    @Test
    void validateShouldReturnErrorIfResponseDataIsNull() {
        Response response = new Response();
        ValidationResult actual = response.validate();
        assertEquals("field \"responseData\" must not be null", actual.errorsToString());
    }

    @Test
    void validateShouldReturnEmptyErrorsIfNoPaymentRequisite() {
        Response response = new Response();
        response.setResponseData(new Response.ResponseData());
        ValidationResult actual = response.validate();
        assertTrue(actual.errorsToString().isEmpty());
    }

    @Test
    void validateShouldReturnErrorIfPaymentRequisitesRequisitesIsNull() {
        Response response = new Response();
        Response.ResponseData responseData = new Response.ResponseData();
        Response.ResponseData.PaymentRequisites paymentRequisites = new Response.ResponseData.PaymentRequisites();
        paymentRequisites.setPaymentMethod("method");
        responseData.setPaymentRequisites(paymentRequisites);
        Response.ResponseData.Transaction transaction = new Response.ResponseData.Transaction();
        transaction.setId("id");
        transaction.setStatus(Status.CANCELLED);
        responseData.setTransaction(transaction);
        response.setResponseData(responseData);
        ValidationResult actual = response.validate();
        assertEquals("field \"responseData.paymentRequisites.requisites\" must not be null", actual.errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfPaymentRequisitesPaymentMethodIsNull() {
        Response response = new Response();
        Response.ResponseData responseData = new Response.ResponseData();
        Response.ResponseData.PaymentRequisites paymentRequisites = new Response.ResponseData.PaymentRequisites();
        paymentRequisites.setRequisites("requisites");
        responseData.setPaymentRequisites(paymentRequisites);
        Response.ResponseData.Transaction transaction = new Response.ResponseData.Transaction();
        transaction.setId("id");
        transaction.setStatus(Status.CANCELLED);
        responseData.setTransaction(transaction);
        response.setResponseData(responseData);
        ValidationResult actual = response.validate();
        assertEquals("field \"responseData.paymentRequisites.paymentMethod\" must not be null", actual.errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfTransactionIsNull() {
        Response response = new Response();
        Response.ResponseData responseData = new Response.ResponseData();
        Response.ResponseData.PaymentRequisites paymentRequisites = new Response.ResponseData.PaymentRequisites();
        paymentRequisites.setRequisites("requisites");
        paymentRequisites.setPaymentMethod("method");
        responseData.setPaymentRequisites(paymentRequisites);
        response.setResponseData(responseData);
        ValidationResult actual = response.validate();
        assertEquals("field \"responseData.transaction\" must not be null", actual.errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfTransactionIdIsNull() {
        Response response = new Response();
        Response.ResponseData responseData = new Response.ResponseData();
        Response.ResponseData.PaymentRequisites paymentRequisites = new Response.ResponseData.PaymentRequisites();
        paymentRequisites.setRequisites("requisites");
        paymentRequisites.setPaymentMethod("method");
        responseData.setPaymentRequisites(paymentRequisites);
        Response.ResponseData.Transaction transaction = new Response.ResponseData.Transaction();
        transaction.setStatus(Status.CANCELLED);
        responseData.setTransaction(transaction);
        response.setResponseData(responseData);
        ValidationResult actual = response.validate();
        assertEquals("field \"responseData.transaction.id\" must not be null", actual.errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfTransactionStatusIsNull() {
        Response response = new Response();
        Response.ResponseData responseData = new Response.ResponseData();
        Response.ResponseData.PaymentRequisites paymentRequisites = new Response.ResponseData.PaymentRequisites();
        paymentRequisites.setRequisites("requisites");
        paymentRequisites.setPaymentMethod("method");
        responseData.setPaymentRequisites(paymentRequisites);
        Response.ResponseData.Transaction transaction = new Response.ResponseData.Transaction();
        transaction.setId("id");
        responseData.setTransaction(transaction);
        response.setResponseData(responseData);
        ValidationResult actual = response.validate();
        assertEquals("field \"responseData.transaction.status\" must not be null", actual.errorsToString());
    }

    @Test
    void hasDetailsShouldReturnTrueIfPaymentRequisitesNotNull() {
        Response response = new Response();
        Response.ResponseData responseData = new Response.ResponseData();
        responseData.setPaymentRequisites(new Response.ResponseData.PaymentRequisites());
        response.setResponseData(responseData);
        assertTrue(response.hasDetails());
    }

    @Test
    void hasDetailsShouldReturnFalseIfPaymentRequisitesIsNull() {
        Response response = new Response();
        Response.ResponseData responseData = new Response.ResponseData();
        response.setResponseData(responseData);
        assertFalse(response.hasDetails());
    }
}