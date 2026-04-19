package tgb.cryptoexchange.merchantdetails.details.gambit;

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
        response.setId("123");
        response.setStatus(Status.AWAITING_FUNDS);
        response.setAmount(322.0);

        Response.Requisites requisites = new Response.Requisites();
        requisites.setCardNumber("12345678");
        requisites.setBankName("ALFA Bank");
        response.setPaymentDetails(requisites);

        assertTrue(response.validate().errorsToString().isEmpty());
    }

    @Test
    void validateShouldReturnNoErrorsIfPhoneNumberFieldsPresent() {
        Response response = new Response();
        response.setId("123");
        response.setStatus(Status.AWAITING_FUNDS);
        response.setAmount(322.0);

        Response.Requisites requisites = new Response.Requisites();
        requisites.setPhone("88005553535");
        requisites.setBankName("ALFA Bank");
        response.setPaymentDetails(requisites);

        assertTrue(response.validate().errorsToString().isEmpty());
    }

    @Test
    void validateShouldReturnErrorIfIdIsNull() {
        Response response = new Response();
        response.setStatus(Status.AWAITING_FUNDS);
        response.setAmount(322.0);

        Response.Requisites requisites = new Response.Requisites();
        requisites.setCardNumber("12345678");
        requisites.setBankName("ALFA Bank");
        response.setPaymentDetails(requisites);
        assertEquals("field \"id\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfStatusIsNull() {
        Response response = new Response();
        response.setId("123");
        response.setAmount(322.0);

        Response.Requisites requisites = new Response.Requisites();
        requisites.setCardNumber("12345678");
        requisites.setBankName("ALFA Bank");
        response.setPaymentDetails(requisites);
        assertEquals("field \"status\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfAmountIsNull() {
        Response response = new Response();
        response.setId("123");
        response.setStatus(Status.AWAITING_FUNDS);

        Response.Requisites requisites = new Response.Requisites();
        requisites.setCardNumber("12345678");
        requisites.setBankName("ALFA Bank");
        response.setPaymentDetails(requisites);
        assertEquals("field \"amount\" must not be null", response.validate().errorsToString());
    }

    @Test
    void shouldFailWhenRequisitesPresentButEmpty() {
        Response response = new Response();
        response.setId("123");
        response.setStatus(Status.AWAITING_FUNDS);
        response.setAmount(322.0);
        response.setPaymentDetails(new Response.Requisites());

        ValidationResult result = response.validate();
        assertFalse(result.isValid());
    }

    @Test
    void shouldPassWhenOnlyPhoneNumberInRequisites() {
        Response response = new Response();
        response.setId("123");
        response.setStatus(Status.AWAITING_FUNDS);
        response.setAmount(322.0);

        Response.Requisites requisites = new Response.Requisites();
        requisites.setCardNumber("1234123412341234");
        requisites.setBankName("ALFA Bank");
        response.setPaymentDetails(requisites);

        ValidationResult result = response.validate();
        assertTrue(result.isValid(), "Должен быть валидным, когда есть телефон и банк");
    }

    @Test
    void shouldPassWhenRecipientPhoneNumberInRequisites() {
        Response response = new Response();
        response.setId("123");
        response.setStatus(Status.AWAITING_FUNDS);
        response.setAmount(322.0);

        Response.Requisites requisites = new Response.Requisites();
        requisites.setPhone("88005553535");
        requisites.setOperator("MTS");
        response.setPaymentDetails(requisites);

        ValidationResult result = response.validate();
        assertTrue(result.isValid(), "Должен быть валидным, когда есть оператор и телефон");
    }

    @Test
    void shouldBeValidWithoutRequisites() {
        Response response = new Response();
        response.setId("123");
        response.setStatus(Status.AWAITING_FUNDS);
        response.setAmount(322.0);
        response.setPaymentDetails(null);

        ValidationResult result = response.validate();
        assertTrue(result.isValid(), "Если реквизитов нет, валидация вложенных полей не должна запускаться");
    }
}