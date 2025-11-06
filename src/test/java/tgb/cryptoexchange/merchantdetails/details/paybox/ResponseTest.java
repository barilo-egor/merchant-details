package tgb.cryptoexchange.merchantdetails.details.paybox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
class ResponseTest {

    @Test
    void validateShouldReturnErrorIfCodeNonNull() {
        Response response = new Response();
        response.setCode("1");
        assertEquals("field \"code\" must be empty but was \"1\"", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfMessageNonNull() {
        Response response = new Response();
        response.setMessage("error occurred");
        assertEquals("field \"message\" must be empty but was \"error occurred\"", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfErrorsNotEmpty() {
        Response response = new Response();
        List<Response.Error> errors = new ArrayList<>();
        response.setErrors(errors);
        Response.Error error = new Response.Error();
        error.setField(List.of("some error", "wrong amount"));
        errors.add(error);
        Response.Error error2 = new Response.Error();
        error2.setField(List.of("no trader"));
        errors.add(error2);
        assertEquals(
                "field \"errors\" expected empty but was: some error, wrong amount, no trader",
                response.validate().errorsToString()
        );
    }

    @Test
    void validateShouldReturnErrorIfIdIsNull() {
        Response response = new Response();
        response.setBankName("bankName");
        response.setStatus(Status.PROCESS);
        response.setPhoneNumber("phoneNumber");
        assertEquals("field \"id\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfBankNameIsNull() {
        Response response = new Response();
        response.setId(123L);
        response.setStatus(Status.PROCESS);
        response.setPhoneNumber("phoneNumber");
        assertEquals("field \"bankName\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfStatusIsNull() {
        Response response = new Response();
        response.setId(123L);
        response.setBankName("bankName");
        response.setPhoneNumber("phoneNumber");
        assertEquals("field \"status\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfPhoneAndCardNumbersIsNull() {
        Response response = new Response();
        response.setId(123L);
        response.setStatus(Status.PROCESS);
        response.setBankName("bankName");
        assertEquals("field \"phoneNumber or cardNumber\" must not be null", response.validate().errorsToString());
    }

    @Test
    void hasDetailsShouldReturnFalseIfCodeNonNull() {
        Response response = new Response();
        response.setCode("code");
        assertFalse(response.hasDetails());
    }

    @Test
    void hasDetailsShouldReturnFalseIfMessageNonNull() {
        Response response = new Response();
        response.setMessage("message");
        assertFalse(response.hasDetails());
    }

    @Test
    void hasDetailsShouldReturnFalseIfErrorsNonEmpty() {
        Response response = new Response();
        response.setErrors(List.of(new Response.Error()));
        assertFalse(response.hasDetails());
    }
}