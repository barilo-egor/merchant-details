package tgb.cryptoexchange.merchantdetails.details.paybox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.CancelOrderRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.OrderCreationRequest;
import tgb.cryptoexchange.merchantdetails.properties.HellBitReceiptProperties;
import tgb.cryptoexchange.merchantdetails.service.RequestService;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HellBitReceiptOrderCreationServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private HellBitReceiptProperties hellBitProperties;

    @Mock
    private RequestService requestService;

    @InjectMocks
    private HellBitReceiptOrderCreationService service;

    @Captor
    private ArgumentCaptor<Function<UriBuilder, URI>> uriBuilderCaptor;

    @Captor
    private ArgumentCaptor<Consumer<HttpHeaders>> headersCaptor;

    @Captor
    private ArgumentCaptor<BodyInserters.MultipartInserter> bodyInserterCaptor;

    @Test
    void getMerchantShouldReturnHellBitReceipt() {
        assertEquals(Merchant.HELLBIT_RECEIPT, service.getMerchant());
    }

    @EnumSource(Method.class)
    @ParameterizedTest
    void uriBuilderShouldSetPathDependsOnMethod(Method method) {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setMethod(method.name());
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();

        assertEquals("/api/v1/transactions" + method.getUri(), service.uriBuilder(detailsRequest).apply(uriBuilder).getPath());
    }

    @ValueSource(strings = {
            "ixoQTh3Gf8mY3ik", "6pm5Px76Dyt5VXW65DjnlFgFnwO0f5II"
    })
    @ParameterizedTest
    void headersShouldSetRequiredHeaders(String token) {
        when(hellBitProperties.token()).thenReturn(token);
        HttpHeaders headers = new HttpHeaders();
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setMethod(Method.CARD.name());
        service.headers(detailsRequest, null).accept(headers);
        assertAll(
                () -> assertEquals("Bearer " + token, Objects.requireNonNull(headers.get("Authorization")).getFirst()),
                () -> assertEquals("application/json", Objects.requireNonNull(headers.get("Content-Type")).getFirst())
        );
    }

    @ValueSource(ints = {
            1211, 5004, 522
    })
    @ParameterizedTest
    void bodyShouldBuildRequestObject(int amount) {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setMethod(Method.CARD.name());
        Request actual = service.body(detailsRequest);

        assertAll(
                () -> assertEquals(amount, actual.getAmount()),
                () -> assertDoesNotThrow(() -> UUID.fromString(actual.getMerchantTransactionId())),
                () -> assertNull(actual.getPayerBankName())
        );
    }

    @Test
    void bodyShouldSetPayerBankNameForAccountPsb() {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setAmount(1000);
        detailsRequest.setMethod(Method.ACCOUNT_PSB.name());
        Request actual = service.body(detailsRequest);

        assertAll(
                () -> assertEquals(1000, actual.getAmount()),
                () -> assertDoesNotThrow(() -> UUID.fromString(actual.getMerchantTransactionId())),
                () -> assertEquals(Method.ACCOUNT_PSB.getBankName(), actual.getPayerBankName())
        );
    }

    @CsvSource({
            "301,https://pay.example.com/receipt-link1",
            "302,https://pay.example.com/receipt-link2"
    })
    @ParameterizedTest
    void buildResponseShouldSetQrForPaymentLink(Long id, String qrLink) {
        Response response = new Response();
        response.setId(id);
        response.setPaymentLink(qrLink);

        Optional<DetailsResponse> maybeRequisiteResponse = service.buildResponse(response);
        assertTrue(maybeRequisiteResponse.isPresent());
        DetailsResponse detailsResponse = maybeRequisiteResponse.get();
        assertAll(
                () -> assertEquals(Merchant.HELLBIT_RECEIPT, detailsResponse.getMerchant()),
                () -> assertEquals(id.toString(), detailsResponse.getMerchantOrderId()),
                () -> assertEquals(Status.PROCESS.name(), detailsResponse.getMerchantOrderStatus()),
                () -> assertEquals(qrLink, detailsResponse.getQr()),
                () -> assertNull(detailsResponse.getBank()),
                () -> assertNull(detailsResponse.getDetails())
        );
    }

    @Test
    void buildResponseWithoutPaymentLinkShouldNotSetQr() {
        Response response = new Response();
        response.setId(303L);

        Optional<DetailsResponse> maybeRequisiteResponse = service.buildResponse(response);
        assertTrue(maybeRequisiteResponse.isPresent());
        DetailsResponse detailsResponse = maybeRequisiteResponse.get();
        assertAll(
                () -> assertEquals(Merchant.HELLBIT_RECEIPT, detailsResponse.getMerchant()),
                () -> assertEquals("303", detailsResponse.getMerchantOrderId()),
                () -> assertEquals(Status.PROCESS.name(), detailsResponse.getMerchantOrderStatus()),
                () -> assertNull(detailsResponse.getQr()),
                () -> assertNull(detailsResponse.getBank()),
                () -> assertNull(detailsResponse.getDetails())
        );
    }

    @CsvSource("""
            4be41169-2786-48f3-98b9-002a23417c45,CARD
            578f16e0-5941-4330-80c3-b2d22ef302b8,ACCOUNT_PSB
            """)
    @ParameterizedTest
    void makeCancelRequestShouldMakeRequest(String orderId, Method method) {
        service.setRequestService(requestService);
        CancelOrderRequest cancelOrderRequest = new CancelOrderRequest();
        cancelOrderRequest.setOrderId(orderId);
        cancelOrderRequest.setMethod(method.name());
        service.makeCancelRequest(cancelOrderRequest);
        verify(requestService).request(eq(webClient), eq(HttpMethod.POST), uriBuilderCaptor.capture(),
                any(), eq(null));
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/api/v1/transactions/" + orderId + "/cancel", uriBuilderCaptor.getValue().apply(uriBuilder).getPath());
    }

    @ValueSource(strings = {"123456", "9876543"})
    @ParameterizedTest
    void sendReceiptShouldCallRequestServiceMethod(String orderId) {
        service.setRequestService(requestService);
        when(hellBitProperties.token()).thenReturn("token");

        service.sendReceipt(orderId, "pdf-content".getBytes(), "receipt.pdf");

        verify(requestService).request(
                eq(webClient),
                eq(HttpMethod.POST),
                uriBuilderCaptor.capture(),
                headersCaptor.capture(),
                bodyInserterCaptor.capture(),
                any()
        );

        URI uri = uriBuilderCaptor.getValue().apply(UriComponentsBuilder.newInstance());
        HttpHeaders headers = new HttpHeaders();
        headersCaptor.getValue().accept(headers);

        assertAll(
                () -> assertTrue(uri.getPath().endsWith("/attach"), "Путь должен заканчиваться на /attach"),
                () -> assertEquals("Bearer token", headers.getFirst("Authorization")),
                () -> assertNotNull(bodyInserterCaptor.getValue(), "BodyInserter не должен быть null")
        );
    }

    @ValueSource(strings = {"123456", "9876543"})
    @ParameterizedTest
    void sendReceiptWithMultipartFileShouldCallRequestServiceMethod(String orderId) {
        service.setRequestService(requestService);
        when(hellBitProperties.token()).thenReturn("token");
        MockMultipartFile multipartFile = new MockMultipartFile(
                "receipts",
                "receipt.pdf",
                "application/pdf",
                "pdf-content".getBytes()
        );

        service.sendReceipt(orderId, multipartFile);

        verify(requestService).request(
                eq(webClient),
                eq(HttpMethod.POST),
                uriBuilderCaptor.capture(),
                headersCaptor.capture(),
                bodyInserterCaptor.capture(),
                any()
        );

        URI uri = uriBuilderCaptor.getValue().apply(UriComponentsBuilder.newInstance());
        HttpHeaders headers = new HttpHeaders();
        headersCaptor.getValue().accept(headers);

        assertAll(
                () -> assertTrue(uri.getPath().endsWith("/attach"), "Путь должен заканчиваться на /attach"),
                () -> assertEquals("Bearer token", headers.getFirst("Authorization")),
                () -> assertNotNull(bodyInserterCaptor.getValue(), "BodyInserter не должен быть null")
        );
    }
}
