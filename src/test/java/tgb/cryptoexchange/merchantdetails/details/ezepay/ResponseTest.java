package tgb.cryptoexchange.merchantdetails.details.ezepay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ResponseTest {

    @CsvSource(value = {
            "error,can't trade for this amount",
            "warning,merchant is off",
            "null, null"
    })
    @ParameterizedTest
    void validateShouldReturnErrorIfStatusNotSuccess(String status, String message) {
        Response response = new Response();
        response.setStatus(status);
        response.setMessage(message);
        assertEquals(
                "field \"status\" expected success, got " + status + " with message \"" + message + "\"",
                response.validate().errorsToString()
        );
    }

    @Test
    void validateShouldReturnErrorIfStatusIsNull() {
        Response response = new Response();
        assertEquals("field \"status\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfDataIsNull() {
        Response response = new Response();
        response.setStatus("success");
        assertEquals("field \"data\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfDataOrderIdIsNull() {
        Response response = new Response();
        response.setStatus("success");
        Response.Data data = new Response.Data();
        response.setData(data);
        data.setBank("bank");
        data.setDetails("details");
        assertEquals("field \"data.orderId\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfBankAndBankSboIsNull() {
        Response response = new Response();
        response.setStatus("success");
        Response.Data data = new Response.Data();
        response.setData(data);
        data.setOrderId("orderId");
        data.setDetails("details");
        assertEquals("field \"data.bank or data.bankSbp\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfDetailsIsNull() {
        Response response = new Response();
        response.setStatus("success");
        Response.Data data = new Response.Data();
        response.setData(data);
        data.setOrderId("orderId");
        data.setBank("bank");
        assertEquals("field \"data.details\" must not be null", response.validate().errorsToString());
    }

    @Test
    void hasDetailsShouldReturnTrue() {
        assertTrue(new Response().hasDetails());
    }
}