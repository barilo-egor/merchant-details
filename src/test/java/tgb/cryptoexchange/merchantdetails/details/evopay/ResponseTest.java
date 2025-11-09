package tgb.cryptoexchange.merchantdetails.details.evopay;

import org.junit.jupiter.api.Test;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {

    @Test
    void validateShouldReturnErrorIfIdIsNull() {
        Response response = new Response();
        response.setOrderStatus(Status.APPEAL);
        Response.Requisites requisites = new Response.Requisites();
        requisites.setRecipientCardNumber("card number");
        requisites.setRecipientBank("bank");
        response.setRequisites(requisites);
        ValidationResult actual = response.validate();
        assertEquals("field \"id\" must not be null", actual.errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfOrderStatusIsNull() {
        Response response = new Response();
        response.setId("id");
        Response.Requisites requisites = new Response.Requisites();
        requisites.setRecipientCardNumber("card number");
        requisites.setRecipientBank("bank");
        response.setRequisites(requisites);
        ValidationResult actual = response.validate();
        assertEquals("field \"orderStatus\" must not be null", actual.errorsToString());
    }

    @Test
    void validateShouldReturnNoErrorsIfRequisitesIsNull() {
        Response response = new Response();
        response.setId("id");
        response.setOrderStatus(Status.APPEAL);
        ValidationResult actual = response.validate();
        assertTrue(actual.errorsToString().isEmpty());
    }

    @Test
    void validateShouldReturnErrorIfPhoneAndCardNumbersIsNull() {
        Response response = new Response();
        response.setId("id");
        response.setOrderStatus(Status.APPEAL);
        Response.Requisites requisites = new Response.Requisites();
        requisites.setRecipientBank("bank");
        response.setRequisites(requisites);
        ValidationResult actual = response.validate();
        assertEquals(
                "field \"recipientPhoneNumber or recipientCardNumber\" must not be null",
                actual.errorsToString()
        );
    }

    @Test
    void validateShouldReturnNoErrorsIfRecipientPhoneNumberNotNull() {
        Response response = new Response();
        response.setId("id");
        response.setOrderStatus(Status.APPEAL);
        Response.Requisites requisites = new Response.Requisites();
        requisites.setRecipientPhoneNumber("card number");
        requisites.setRecipientBank("bank");
        response.setRequisites(requisites);
        assertTrue(response.validate().errorsToString().isEmpty());
    }

    @Test
    void validateShouldReturnErrorIfBankIsNull() {
        Response response = new Response();
        response.setId("id");
        response.setOrderStatus(Status.APPEAL);
        Response.Requisites requisites = new Response.Requisites();
        requisites.setRecipientCardNumber("card number");
        response.setRequisites(requisites);
        ValidationResult actual = response.validate();
        assertEquals(
                "field \"recipientBank\" must not be null",
                actual.errorsToString()
        );
    }

    @Test
    void validateShouldReturnNoErrorsIfAllFieldsPresent() {
        Response response = new Response();
        response.setId("id");
        response.setOrderStatus(Status.CREATED);
        Response.Requisites requisites = new Response.Requisites();
        response.setRequisites(requisites);
        requisites.setRecipientCardNumber("card number");
        requisites.setRecipientBank("bank");
        assertTrue(response.validate().errorsToString().isEmpty());
    }

    @Test
    void hasDetailsShouldReturnTrueIfRequisitesNotNull() {
        Response response = new Response();
        response.setRequisites(new Response.Requisites());
        assertTrue(response.hasDetails());
    }

    @Test
    void hasDetailsShouldReturnFalseIfRequisitesNull() {
        Response response = new Response();
        assertFalse(response.hasDetails());
    }
}