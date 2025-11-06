package tgb.cryptoexchange.merchantdetails.details.evopay;

import org.junit.jupiter.api.Test;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void validateShouldReturnErrorIfRequisitesIsNull() {
        Response response = new Response();
        response.setId("id");
        response.setOrderStatus(Status.APPEAL);
        ValidationResult actual = response.validate();
        assertEquals("field \"requisites\" must not be null", actual.errorsToString());
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
    void hasDetailsShouldReturnTrue() {
        Response response = new Response();
        assertTrue(response.hasDetails());
    }
}