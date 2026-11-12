package tgb.cryptoexchange.merchantdetails.details.asgard;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ResponseTest {

    @Test
    void validateShouldReturnNoErrorsIfCardNumberFieldsPresent() {
        Response response = new Response();
        Response.Requisites requisites = new Response.Requisites();
        requisites.setId("123");
        requisites.setAmount(322.0);
        requisites.setState(Status.CREATED);
        requisites.setMethod(Method.CARD);
        requisites.setAddress("12345678");
        requisites.setBankName("ALFA Bank");
        response.setRequisites(requisites);

        assertTrue(response.validate().errorsToString().isEmpty());
    }

    @Test
    void validateShouldReturnErrorIfIdIsNull() {
        Response response = new Response();
        Response.Requisites requisites = new Response.Requisites();
        requisites.setAmount(322.0);
        requisites.setState(Status.CREATED);
        requisites.setMethod(Method.CARD);
        requisites.setAddress("12345678");
        requisites.setBankName("ALFA Bank");
        response.setRequisites(requisites);
        assertEquals("field \"id\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfStatusIsNull() {
        Response response = new Response();
        Response.Requisites requisites = new Response.Requisites();
        requisites.setId("123");
        requisites.setAmount(322.0);
        requisites.setMethod(Method.CARD);
        requisites.setAddress("12345678");
        requisites.setBankName("ALFA Bank");
        response.setRequisites(requisites);
        assertEquals("field \"state\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfAmountIsNull() {
        Response response = new Response();
        Response.Requisites requisites = new Response.Requisites();
        requisites.setId("123");
        requisites.setState(Status.CREATED);
        requisites.setMethod(Method.CARD);
        requisites.setAddress("12345678");
        requisites.setBankName("ALFA Bank");
        response.setRequisites(requisites);
        assertEquals("field \"amount\" must not be null", response.validate().errorsToString());
    }

    @Test
    void shouldFailWhenRequisitesPresentButEmpty() {
        Response response = new Response();
        Response.Requisites requisites = new Response.Requisites();
        response.setRequisites(requisites);

        ValidationResult result = response.validate();
        assertFalse(result.isValid());
    }

    @Test
    void validateShouldReturnErrorIfAddressIsNull() {
        Response response = new Response();
        Response.Requisites requisites = new Response.Requisites();
        requisites.setId("123");
        requisites.setAmount(322.0);
        requisites.setState(Status.CREATED);
        requisites.setMethod(Method.CARD);
        requisites.setBankName("ALFA Bank");
        response.setRequisites(requisites);
        assertEquals("field \"address\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfBankNameIsNull() {
        Response response = new Response();
        Response.Requisites requisites = new Response.Requisites();
        requisites.setId("123");
        requisites.setAmount(322.0);
        requisites.setState(Status.CREATED);
        requisites.setMethod(Method.CARD);
        requisites.setAddress("12345678");
        response.setRequisites(requisites);
        assertEquals("field \"bankName\" must not be null", response.validate().errorsToString());
    }

    @Test
    void shouldBeValidWithoutRequisites() {
        Response response = new Response();
        response.setRequisites(null);

        ValidationResult result = response.validate();
        assertTrue(result.isValid(), "Если реквизитов нет, валидация вложенных полей не должна запускаться");
    }
}