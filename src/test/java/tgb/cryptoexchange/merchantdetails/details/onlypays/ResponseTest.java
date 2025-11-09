package tgb.cryptoexchange.merchantdetails.details.onlypays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ResponseTest {

    @ValueSource(strings = {
            "merchant not found",
            "amount can't be less than 2500"
    })
    @ParameterizedTest
    void validateShouldReturnErrorIfSuccessIsFalse(String error) {
        Response response = new Response();
        response.setSuccess(false);
        response.setError(error);
        assertEquals("field \"success\" expected true, but was false: " + error, response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnNoErrorsIfDataIsNullAndSuccessIsTrue() {
        Response response = new Response();
        response.setSuccess(true);
        assertTrue(response.validate().errorsToString().isEmpty());
    }

    @Test
    void validateShouldReturnErrorIfDataIdIsNull() {
        Response response = new Response();
        response.setSuccess(true);
        Response.Data data = new Response.Data();
        response.setData(data);
        data.setRequisite("requisite");
        data.setBank("bank");
        assertEquals("field \"data.id\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfDataRequisiteIsNull() {
        Response response = new Response();
        response.setSuccess(true);
        Response.Data data = new Response.Data();
        data.setId("id");
        data.setBank("bank");
        response.setData(data);
        assertEquals("field \"data.requisite\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfDataBankIsNull() {
        Response response = new Response();
        response.setSuccess(true);response.setSuccess(true);
        Response.Data data = new Response.Data();
        data.setId("id");
        data.setRequisite("requisite");
        response.setData(data);
        assertEquals("field \"data.bank\" must not be null", response.validate().errorsToString());
    }

    @Test
    void hasDetailsShouldReturnTrueIfDataIsNotNull() {
        Response response = new Response();
        response.setData(new Response.Data());
        assertTrue(response.hasDetails());
    }

    @Test
    void hasDetailsShouldReturnFalseIfDataIsNotNull() {
        Response response = new Response();
        assertFalse(response.hasDetails());
    }
}