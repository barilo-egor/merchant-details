package tgb.cryptoexchange.merchantdetails.details.levelpay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ResponseTest {

    @Test
    void validateShouldReturnErrorIfSuccessIsFalse() {
        Response response = new Response();
        response.setSuccess(false);
        assertEquals("field \"success\" expected true but was false", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfDataIsNull() {
        Response response = new Response();
        response.setSuccess(true);
        assertEquals("field \"data\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfDataOrderIdIsNull() {
        Response response = new Response();
        response.setSuccess(true);
        Response.Order order = new Response.Order();
        order.setStatus(Status.PENDING);
        order.setPaymentGatewayName("paymentGatewayName");
        Response.Order.PaymentDetail paymentDetail = new Response.Order.PaymentDetail();
        paymentDetail.setDetail("detail");
        order.setPaymentDetail(paymentDetail);
        response.setData(order);
        assertEquals("field \"data.orderId\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfStatusIsNull() {
        Response response = new Response();
        response.setSuccess(true);
        Response.Order order = new Response.Order();
        order.setOrderId("orderId");
        order.setPaymentGatewayName("paymentGatewayName");
        Response.Order.PaymentDetail paymentDetail = new Response.Order.PaymentDetail();
        paymentDetail.setDetail("detail");
        order.setPaymentDetail(paymentDetail);
        response.setData(order);
        assertEquals("field \"data.status\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfPaymentGatewayNameAndPaymentGatewayIsNull() {
        Response response = new Response();
        response.setSuccess(true);
        Response.Order order = new Response.Order();
        order.setOrderId("orderId");
        order.setStatus(Status.PENDING);
        Response.Order.PaymentDetail paymentDetail = new Response.Order.PaymentDetail();
        paymentDetail.setDetail("detail");
        order.setPaymentDetail(paymentDetail);
        response.setData(order);
        assertEquals("field \"data.paymentGatewayName or data.paymentGateway\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfPaymentDetailIsNull() {
        Response response = new Response();
        response.setSuccess(true);
        Response.Order order = new Response.Order();
        order.setOrderId("orderId");
        order.setStatus(Status.PENDING);
        order.setPaymentGateway("paymentGateway");
        response.setData(order);
        assertEquals("field \"data.paymentDetail\" must not be null", response.validate().errorsToString());
    }

    @Test
    void validateShouldReturnErrorIfPaymentDetailDetailIsNull() {
        Response response = new Response();
        response.setSuccess(true);
        Response.Order order = new Response.Order();
        order.setOrderId("orderId");
        order.setStatus(Status.PENDING);
        order.setPaymentGatewayName("paymentGatewayName");
        Response.Order.PaymentDetail paymentDetail = new Response.Order.PaymentDetail();
        order.setPaymentDetail(paymentDetail);
        response.setData(order);
        assertEquals("field \"data.paymentDetail.detail\" must not be null", response.validate().errorsToString());
    }

    @Test
    void hasDetailsShouldReturnTrue() {
        assertTrue(new Response().hasDetails());
    }
}