package tgb.cryptoexchange.merchantdetails.details.levelpay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MeridianPayCreationServiceTest {

    @InjectMocks
    private MeridianPayOrderCreationService meridianPayOrderCreationService;

    @Test
    void getMerchantShouldReturnMeridianPay() {
        assertEquals(Merchant.MERIDIAN_PAY, meridianPayOrderCreationService.getMerchant());
    }

    @Test
    void buildResponseShouldSetBankFromGatewayNameAndDetails() {
        Response response = new Response();
        Response.Order order = new Response.Order();
        order.setOrderId("lp-order-1");
        order.setStatus(Status.PENDING);
        order.setAmount("5000");
        order.setPaymentGatewayName("T-Bank");
        order.setPaymentGateway("TBANK");

        Response.Order.PaymentDetail detail = new Response.Order.PaymentDetail();
        detail.setDetail("+79991234567");
        order.setPaymentDetail(detail);
        response.setData(order);

        Optional<DetailsResponse> result = meridianPayOrderCreationService.buildResponse(response);
        assertTrue(result.isPresent());
        DetailsResponse detailsResponse = result.get();

        assertAll(
                () -> assertEquals("T-Bank", detailsResponse.getBank()),
                () -> assertEquals("+79991234567", detailsResponse.getDetails()),
                () -> assertEquals("lp-order-1", detailsResponse.getMerchantOrderId()),
                () -> assertEquals(50, detailsResponse.getAmount()),
                () -> assertEquals(Merchant.MERIDIAN_PAY, detailsResponse.getMerchant())
        );
    }

    @Test
    void buildResponseShouldSetBankFromGatewayWhenGatewayNameNull() {
        Response response = new Response();
        Response.Order order = new Response.Order();
        order.setOrderId("lp-order-2");
        order.setStatus(Status.PENDING);
        order.setAmount("3000");
        order.setPaymentGateway("SBER");

        Response.Order.PaymentDetail detail = new Response.Order.PaymentDetail();
        detail.setDetail("4276123412341234");
        order.setPaymentDetail(detail);
        response.setData(order);

        Optional<DetailsResponse> result = meridianPayOrderCreationService.buildResponse(response);
        assertTrue(result.isPresent());
        DetailsResponse detailsResponse = result.get();

        assertAll(
                () -> assertEquals("SBER", detailsResponse.getBank()),
                () -> assertEquals("4276123412341234", detailsResponse.getDetails())
        );
    }

    @Test
    void buildResponseShouldSetQrWhenDetailIsNull() {
        Response response = new Response();
        Response.Order order = new Response.Order();
        order.setOrderId("lp-order-3");
        order.setStatus(Status.PENDING);
        order.setAmount("2000");

        Response.Order.PaymentDetail detail = new Response.Order.PaymentDetail();
        detail.setQrCodeLink("https://qr.example.com/pay");
        order.setPaymentDetail(detail);
        response.setData(order);

        Optional<DetailsResponse> result = meridianPayOrderCreationService.buildResponse(response);
        assertTrue(result.isPresent());
        DetailsResponse detailsResponse = result.get();

        assertAll(
                () -> assertNull(detailsResponse.getBank()),
                () -> assertNull(detailsResponse.getDetails()),
                () -> assertEquals("https://qr.example.com/pay", detailsResponse.getQr())
        );
    }

}