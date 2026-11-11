package tgb.cryptoexchange.merchantdetails.details.goatx;

import org.junit.jupiter.api.Test;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import static org.junit.jupiter.api.Assertions.*;


class ResponseTest {

    @Test
    void validateShouldReturnErrorIfIdIsNull() {
        Response response = new Response();
        response.setStatus(Status.PENDING);
        response.setWay(Method.SBP);
        response.setRequisite(new Response.Requisite());
        Response.Requisite requisite = new Response.Requisite();
        requisite.setCardNumber("1234 1234 1234 1234");
        Response.Requisite.Bank bank = new Response.Requisite.Bank();
        bank.setName("Сбербанк");
        requisite.setBank(bank);
        response.setRequisite(requisite);
        ValidationResult actual = response.validate();
        assertEquals("field \"id\" must not be null", actual.errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfOrderStatusIsNull() {
        Response response = new Response();
        response.setId("id");
        response.setWay(Method.SBP);
        response.setRequisite(new Response.Requisite());
        Response.Requisite requisite = new Response.Requisite();
        requisite.setCardNumber("1234 1234 1234 1234");
        Response.Requisite.Bank bank = new Response.Requisite.Bank();
        bank.setName("Сбербанк");
        requisite.setBank(bank);
        response.setRequisite(requisite);
        ValidationResult actual = response.validate();
        assertEquals("field \"status\" must not be null", actual.errorsToString());
    }

    @Test
    void validateShouldReturnNoErrorsIfRequisitesIsNull() {
        Response response = new Response();
        response.setId("id");
        response.setStatus(Status.PENDING);
        response.setRequisite(new Response.Requisite());
        Response.Requisite requisite = new Response.Requisite();
        requisite.setCardNumber("1234 1234 1234 1234");
        Response.Requisite.Bank bank = new Response.Requisite.Bank();
        bank.setName("Сбербанк");
        requisite.setBank(bank);
        response.setRequisite(requisite);
        ValidationResult actual = response.validate();
        assertEquals("field \"way\" must not be null", actual.errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfPhoneAndCardNumbersIsNull() {
        Response response = new Response();
        response.setId("id");
        response.setWay(Method.SBP);
        response.setStatus(Status.PENDING);
        Response.Requisite requisite = new Response.Requisite();
        Response.Requisite.Bank bank = new Response.Requisite.Bank();
        bank.setName("Сбербанк");
        requisite.setBank(bank);
        response.setRequisite(requisite);
        ValidationResult actual = response.validate();
        assertEquals("field \"cardNumber or phoneNumber\" must not be null", actual.errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfBankIsNull() {
        Response response = new Response();
        response.setId("id");
        response.setWay(Method.SBP);
        response.setStatus(Status.PENDING);
        Response.Requisite requisite = new Response.Requisite();
        requisite.setCardNumber("1234 1234 1234 1234");
        response.setRequisite(requisite);
        ValidationResult actual = response.validate();
        assertEquals("field \"bank.name\" must not be null", actual.errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfBankNameIsNull() {
        Response response = new Response();
        response.setId("id");
        response.setWay(Method.SBP);
        response.setStatus(Status.PENDING);
        Response.Requisite requisite = new Response.Requisite();
        requisite.setCardNumber("1234 1234 1234 1234");
        Response.Requisite.Bank bank = new Response.Requisite.Bank();
        requisite.setBank(bank);
        response.setRequisite(requisite);
        ValidationResult actual = response.validate();
        assertEquals("field \"bank.name\" must not be null", actual.errorsToString());
    }

    @Test
    void validateShouldReturnNoErrorsIfAllDataPresent() {
        Response response = new Response();
        response.setId("id");
        response.setWay(Method.SBP);
        response.setStatus(Status.PENDING);
        Response.Requisite requisite = new Response.Requisite();
        requisite.setCardNumber("1234 1234 1234 1234");
        Response.Requisite.Bank bank = new Response.Requisite.Bank();
        bank.setName("Сбербанк");
        requisite.setBank(bank);
        response.setRequisite(requisite);
        ValidationResult actual = response.validate();
        assertTrue(actual.errorsToString().isEmpty());
    }

    @Test
    void hasDetailsShouldReturnTrueIfRequisitesNotNull() {
        Response response = new Response();
        response.setRequisite(new Response.Requisite());
        assertTrue(response.hasDetails());
    }

    @Test
    void hasDetailsShouldReturnFalseIfRequisitesNull() {
        Response response = new Response();
        assertFalse(response.hasDetails());
    }
}