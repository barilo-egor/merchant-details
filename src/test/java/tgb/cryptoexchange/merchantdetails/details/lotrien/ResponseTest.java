package tgb.cryptoexchange.merchantdetails.details.lotrien;

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
        response.setStatus(Status.APPEAL);
        response.setPaymentMethod(Method.BANK_CARD);
        response.setAmount("322");

        Response.Requisites requisites = new Response.Requisites();
        requisites.setCardNumber("12345678");
        requisites.setBank("ALFA Bank");
        response.setRequisites(requisites);

        assertTrue(response.validate().errorsToString().isEmpty());
    }

    @Test
    void validateShouldReturnNoErrorsIfPhoneNumberFieldsPresent() {
        Response response = new Response();
        response.setId("123");
        response.setStatus(Status.APPEAL);
        response.setPaymentMethod(Method.SBP);
        response.setAmount("322");

        Response.Requisites requisites = new Response.Requisites();
        requisites.setPhoneNumber("88005553535");
        requisites.setBank("ALFA Bank");
        response.setRequisites(requisites);

        assertTrue(response.validate().errorsToString().isEmpty());
    }

    @Test
    void validateShouldReturnErrorIfIdIsNull() {
        Response response = new Response();
        response.setStatus(Status.APPEAL);
        response.setAmount("322");
        response.setPaymentMethod(Method.BANK_CARD);
        assertEquals("field \"id\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfStatusIsNull() {
        Response response = new Response();
        response.setId("id");
        response.setAmount("322");
        response.setPaymentMethod(Method.BANK_CARD);
        assertEquals("field \"status\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfAmountIsNull() {
        Response response = new Response();
        response.setId("id");
        response.setStatus(Status.APPEAL);
        response.setPaymentMethod(Method.BANK_CARD);
        assertEquals("field \"amount\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfPaymentMethodIsNull() {
        Response response = new Response();
        response.setId("id");
        response.setStatus(Status.APPEAL);
        response.setAmount("322");
        assertEquals("field \"paymentMethod\" must not be null", response.validate().errorsToString());
    }

    @Test
    void shouldFailWhenRequisitesPresentButEmpty() {
        Response response = new Response();
        response.setId("123");
        response.setStatus(Status.APPEAL);
        response.setPaymentMethod(Method.BANK_CARD);
        response.setAmount("322");
        response.setRequisites(new Response.Requisites());

        ValidationResult result = response.validate();
        assertFalse(result.isValid());
    }

    @Test
    void shouldPassWhenOnlyPhoneNumberInRequisites() {
        Response response = new Response();
        response.setId("123");
        response.setRequisites(new Response.Requisites());
        response.getRequisites().setPhoneNumber("88005553535");
        response.getRequisites().setBank("Some Bank");
        response.setStatus(Status.APPEAL);
        response.setPaymentMethod(Method.BANK_CARD);
        response.setAmount("322");

        ValidationResult result = response.validate();
        assertTrue(result.isValid(), "Должен быть валидным, если есть хотя бы телефон или банк");
    }

    @Test
    void shouldBeValidWithoutRequisites() {
        Response response = new Response();
        response.setId("123");
        response.setStatus(Status.CANCEL);
        response.setPaymentMethod(Method.BANK_CARD);
        response.setAmount("322");
        response.setRequisites(null);

        ValidationResult result = response.validate();
        assertTrue(result.isValid(), "Если реквизитов нет, валидация вложенных полей не должна запускаться");
    }
}