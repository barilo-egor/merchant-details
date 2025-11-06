package tgb.cryptoexchange.merchantdetails.details.levelpay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.FoxPaysProperties;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FoxPaysOrderCreationServiceTest {

    @Mock
    private FoxPaysProperties foxPaysProperties;

    @InjectMocks
    private FoxPaysOrderCreationService foxPaysOrderCreationService;

    @Test
    void getMerchantShouldReturnFoxPays() {
        assertEquals(Merchant.FOX_PAYS, foxPaysOrderCreationService.getMerchant());
    }

    @Test
    void uriBuilderShouldAddPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/api/h2h/order", foxPaysOrderCreationService.uriBuilder(null).apply(uriBuilder).getPath());
    }

    @ValueSource(strings = {
            "iXyHJ2zMDavNqGI", "nDHgf5OQX0h5baF"
    })
    @ParameterizedTest
    void headersShouldAddRequiredHeaders(String token) {
        when(foxPaysProperties.token()).thenReturn(token);
        HttpHeaders headers = new HttpHeaders();
        foxPaysOrderCreationService.headers(null, null).accept(headers);
        assertAll(
                () -> assertEquals("application/json", Objects.requireNonNull(headers.get("Content-Type")).getFirst()),
                () -> assertEquals("application/json", Objects.requireNonNull(headers.get("Accept")).getFirst()),
                () -> assertEquals(token, Objects.requireNonNull(headers.get("Access-Token")).getFirst())
        );
    }

    @CsvSource({
            "12500,CARD,https://gateway.paysendmmm.online/merchant/foxpays,BTC24MONEY",
            "2566,PHONE,https://cryptoexchange.com/foxpays/callback,BULBAEXCHANGE"
    })
    @ParameterizedTest
    void bodyShouldBuildRequestObject(Integer amount, Method method, String callbackUrl, String merchantId) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setMethod(method.name());
        detailsRequest.setCallbackUrl(callbackUrl);
        when(foxPaysProperties.merchantId()).thenReturn(merchantId);

        Request request = foxPaysOrderCreationService.body(detailsRequest);
        assertAll(
                () -> assertEquals(amount, request.getAmount()),
                () -> assertEquals(method, request.getPaymentDetailType()),
                () -> assertEquals(callbackUrl, request.getCallbackUrl()),
                () -> assertEquals(merchantId, request.getMerchantId()),
                () -> assertDoesNotThrow(() -> UUID.fromString(request.getExternalId())),
                () -> assertTrue(request.getFloatingAmount())
        );
    }

    @CsvSource({
            "ALFA,1234432112344321,784fd85f-48a9-4521-8d3d-a2bef09b98ba,FAIL,5660",
            "Сбербанк,78965433443,e356e472-1837-4fe3-9a7a-c0fd445663df,PENDING,8321"
    })
    @ParameterizedTest
    void buildResponseShouldBuildResponseObject(String paymentGatewayName, String detail, String orderId, Status status, Integer amount) {
        Response response = new Response();
        Response.Order order = new Response.Order();
        order.setOrderId(orderId);
        order.setStatus(status);
        Response.Order.PaymentDetail paymentDetail = new Response.Order.PaymentDetail();
        paymentDetail.setDetail(detail);
        order.setPaymentDetail(paymentDetail);
        order.setPaymentGatewayName(paymentGatewayName);
        order.setAmount(amount.toString());
        response.setData(order);

        Optional<DetailsResponse> maybeResponse = foxPaysOrderCreationService.buildResponse(response);
        assertTrue(maybeResponse.isPresent());
        DetailsResponse actual = maybeResponse.get();
        assertAll(
            () -> assertEquals(paymentGatewayName + " " + detail, actual.getDetails()),
                () -> assertEquals(Merchant.FOX_PAYS, actual.getMerchant()),
                () -> assertEquals(orderId, actual.getMerchantOrderId()),
                () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(amount, actual.getAmount())
        );
    }
}