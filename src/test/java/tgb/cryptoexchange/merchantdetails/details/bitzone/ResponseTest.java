package tgb.cryptoexchange.merchantdetails.details.bitzone;

import org.junit.jupiter.api.Test;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {

    @Test
    void validateShouldReturnErrorIfIdIsNull() {
        Response response = new Response();
        response.setStatus(Status.DISPUTE);
        response.setMethod(Method.SBP);
        Response.Requisite requisite = new Response.Requisite();
        requisite.setBank("bank");
        requisite.setRequisites("requisites");
        response.setRequisite(requisite);
        ValidationResult actual = response.validate();
        assertAll(
            () -> assertEquals("field \"id\" must not be null", actual.errorsToString())
        );
    }

    @Test
    void validateShouldReturnErrorIfStatusIsNull() {
        Response response = new Response();
        response.setId("id");
        response.setMethod(Method.SBP);
        Response.Requisite requisite = new Response.Requisite();
        requisite.setBank("bank");
        requisite.setRequisites("requisites");
        response.setRequisite(requisite);
        ValidationResult actual = response.validate();
        assertAll(
                () -> assertEquals("field \"status\" must not be null", actual.errorsToString())
        );
    }

    @Test
    void validateShouldReturnErrorIfMethodIsNull() {
        Response response = new Response();
        response.setId("id");
        response.setStatus(Status.DISPUTE);
        Response.Requisite requisite = new Response.Requisite();
        requisite.setBank("bank");
        requisite.setRequisites("requisites");
        response.setRequisite(requisite);
        ValidationResult actual = response.validate();
        assertAll(
                () -> assertEquals("field \"method\" must not be null", actual.errorsToString())
        );
    }

    @Test
    void validateShouldReturnErrorIfRequisiteIsNull() {
        Response response = new Response();
        response.setId("id");
        response.setStatus(Status.DISPUTE);
        response.setMethod(Method.SBP);
        ValidationResult actual = response.validate();
        assertAll(
                () -> assertEquals("field \"requisite\" must not be null", actual.errorsToString())
        );
    }

    @Test
    void validateShouldReturnErrorIfRequisiteBankIsNull() {
        Response response = new Response();
        response.setId("id");
        response.setStatus(Status.DISPUTE);
        response.setMethod(Method.SBP);
        Response.Requisite requisite = new Response.Requisite();
        requisite.setRequisites("requisites");
        response.setRequisite(requisite);
        ValidationResult actual = response.validate();
        assertAll(
                () -> assertEquals("field \"requisite.bank\" must not be null", actual.errorsToString())
        );
    }

    @Test
    void validateShouldReturnErrorIfRequisiteRequisitesAndSbpNumberIsNull() {
        Response response = new Response();
        response.setId("id");
        response.setStatus(Status.DISPUTE);
        response.setMethod(Method.SBP);
        Response.Requisite requisite = new Response.Requisite();
        requisite.setBank("bank");
        response.setRequisite(requisite);
        ValidationResult actual = response.validate();
        assertAll(
                () -> assertEquals("field \"requisite.requisites or requisite.sbpNumber\" must not be null", actual.errorsToString())
        );
    }

    @Test
    void validateShouldReturnEmptyErrors() {
        Response response = new Response();
        response.setId("id");
        response.setStatus(Status.DISPUTE);
        response.setMethod(Method.SBP);
        Response.Requisite requisite = new Response.Requisite();
        requisite.setBank("bank");
        requisite.setSbpNumber("sbpNumber");
        response.setRequisite(requisite);
        ValidationResult actual = response.validate();
        assertTrue(actual.isValid());
    }

    @Test
    void validateShouldReturnTrue() {
        Response response = new Response();
        assertTrue(response.hasDetails());
    }
}