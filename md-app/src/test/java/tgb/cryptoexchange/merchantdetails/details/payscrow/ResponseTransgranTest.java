package tgb.cryptoexchange.merchantdetails.details.payscrow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResponseTransgranTest {

    @Test
    void validateShouldReturnErrorIfSuccessIsFalse() {
        ResponseTransgran response = new ResponseTransgran();
        response.setSuccess(false);
        assertEquals("field \"success\" expected 'true' but was false", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfOrderIdIsNull() {
        ResponseTransgran response = createValidResponse();
        response.getData().getOrderData().setOrderId(null);
        assertEquals("field \"data.orderData.orderId\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfAmountIsNull() {
        ResponseTransgran response = createValidResponse();
        response.getData().getOrderData().setAmount(null);
        assertEquals("field \"data.orderData.amount\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnNoErrorsIfAllFieldsPresent() {
        ResponseTransgran response = createValidResponse();
        assertTrue(response.validate().errorsToString().isEmpty());
    }

    @Test
    void hasDetailsShouldReturnTrueWhenDataFormUrlAndOrderDataPresent() {
        ResponseTransgran response = createValidResponse();
        assertTrue(response.hasDetails());
    }

    @Test
    void hasDetailsShouldReturnFalseIfDataIsNull() {
        ResponseTransgran response = new ResponseTransgran();
        response.setData(null);
        assertFalse(response.hasDetails());
    }

    @Test
    void hasDetailsShouldReturnFalseIfFormUrlIsNull() {
        ResponseTransgran response = createValidResponse();
        response.getData().setFormUrl(null);
        assertFalse(response.hasDetails());
    }

    @Test
    void hasDetailsShouldReturnFalseIfOrderDataIsNull() {
        ResponseTransgran response = createValidResponse();
        response.getData().setOrderData(null);
        assertFalse(response.hasDetails());
    }

    private ResponseTransgran createValidResponse() {
        ResponseTransgran response = new ResponseTransgran();
        response.setSuccess(true);

        ResponseTransgran.Data data = new ResponseTransgran.Data();
        data.setFormUrl("https://pay.payscrow.io/form/test");

        ResponseTransgran.Data.OrderData orderData = new ResponseTransgran.Data.OrderData();
        orderData.setOrderId("test-order-id");
        orderData.setAmount(1000.0);

        data.setOrderData(orderData);
        response.setData(data);
        return response;
    }
}
