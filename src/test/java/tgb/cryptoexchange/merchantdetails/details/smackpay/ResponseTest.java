package tgb.cryptoexchange.merchantdetails.details.smackpay;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {

    @Test
    void validateShouldReturnErrorIdNull() {
        Response response = new Response();
        response.setAmount(1000);
        response.setStatus(Status.PENDING);
        response.setPaymentUrl("https://pay.smackpay.com/invoice/123");

        assertTrue(response.validate().errorsToString().contains("field \"id\" must not be null"));
    }

    @Test
    void validateShouldReturnErrorAmountNull() {
        Response response = new Response();
        response.setId("order-123");
        response.setStatus(Status.PENDING);
        response.setPaymentUrl("https://pay.smackpay.com/invoice/123");

        assertTrue(response.validate().errorsToString().contains("field \"amount\" must not be null"));
    }

    @Test
    void validateShouldReturnErrorStatusNull() {
        Response response = new Response();
        response.setId("order-123");
        response.setAmount(1000);
        response.setPaymentUrl("https://pay.smackpay.com/invoice/123");

        assertEquals("field \"status\" must not be null", response.validate().errorsToString());
    }

    @Test
    void hasDetailsShouldReturnTrueWhenPaymentUrlPresent() {
        Response response = new Response();
        response.setPaymentUrl("https://pay.smackpay.com/invoice/123");

        assertTrue(response.hasDetails());
    }

    @Test
    void hasDetailsShouldReturnFalseWhenPaymentUrlIsNull() {
        Response response = new Response();
        response.setPaymentUrl(null);

        assertFalse(response.hasDetails());
    }
}