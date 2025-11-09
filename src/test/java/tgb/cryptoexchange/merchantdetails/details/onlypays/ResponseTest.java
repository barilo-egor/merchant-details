package tgb.cryptoexchange.merchantdetails.details.onlypays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ResponseTest {

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
    void hasDetailsShouldReturnFalseIfDataIsNotNull() {
        Response response = new Response();
        assertFalse(response.hasDetails());
    }

    @Test
    void validateShouldReturnErrorIfErrorNotNullAndNotNoAvailableRequisites() {
        Response response = new Response();
        response.setSuccess(false);
        response.setError("Some error");
        assertEquals("field \"error\" must be empty but was \"Some error\"", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnNoErrorIfNoAvailableRequisitesError() {
        Response response = new Response();
        response.setSuccess(false);
        response.setError("No available requisites");
        assertTrue(response.validate().errorsToString().isEmpty());
    }

    @Test
    void validateShouldReturnNoErrorIfErrorIsNull() {
        Response response = new Response();
        response.setSuccess(false);
        assertTrue(response.validate().errorsToString().isEmpty());
    }
}