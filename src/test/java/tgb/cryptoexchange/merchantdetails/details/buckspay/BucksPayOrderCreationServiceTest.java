package tgb.cryptoexchange.merchantdetails.details.buckspay;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.CancelOrderRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.BucksPayPropertiesImpl;
import tgb.cryptoexchange.merchantdetails.service.RequestService;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BucksPayOrderCreationServiceTest {

    @Mock
    private BucksPayPropertiesImpl properties;

    private BucksPayOrderCreationServiceImpl service;

    @Mock
    private RequestService requestService;

    @Mock
    private CallbackConfig callbackConfig;

    @Mock
    private SignatureService signatureService;

    @Mock
    private WebClient webClient;

    @Captor
    private ArgumentCaptor<String> textCaptor;

    @Captor
    private ArgumentCaptor<String> secretCaptor;

    @BeforeEach
    void setUp() {
        service = new BucksPayOrderCreationServiceImpl(
                webClient, properties, callbackConfig, signatureService
        );
        service.setRequestService(requestService);
    }

    @Test
    void testUriBuilder_ShouldReturnCorrectPath() {
        DetailsRequest request = new DetailsRequest();
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();

        Function<UriBuilder, URI> uriFunction = service.uriBuilder(request);
        URI resultUri = uriFunction.apply(uriBuilder);

        assertEquals("/invoices/set", resultUri.getPath());
    }

    @Test
    void testHeaders_ShouldAddRequiredHeadersWithSignature_ForDefaultMethod() {
        DetailsRequest request = new DetailsRequest();
        request.setCurrentMerchantMethod(Method.CARD.name());
        HttpHeaders headers = new HttpHeaders();

        when(properties.key()).thenReturn("key");
        when(properties.secret()).thenReturn("secret");

        when(signatureService.hmacSHA256(anyString(), anyString())).thenReturn("mocked_signature_hash");

        Consumer<HttpHeaders> headersConsumer = service.headers(request, "{}");
        headersConsumer.accept(headers);

        verify(signatureService).hmacSHA256(textCaptor.capture(), secretCaptor.capture());

        String actualTextPassedToHash = textCaptor.getValue();
        String actualSecretPassedToHash = secretCaptor.getValue();

        assertEquals("secret", actualSecretPassedToHash);

        assertTrue(actualTextPassedToHash.startsWith("key"));

        assertNotNull(headers.getFirst("NONCE"));
        assertEquals("application/json", headers.getFirst("Content-Type"));
        assertEquals("key", headers.getFirst("APIKEY"));
        assertEquals("MOCKED_SIGNATURE_HASH", headers.getFirst("SIGNATURE"));
    }

    @Test
    void testHeaders_ShouldUseQrKeysAndSecret_WhenMethodIsNspk() {
        DetailsRequest request = new DetailsRequest();
        request.setCurrentMerchantMethod(Method.NSPK.name());
        HttpHeaders headers = new HttpHeaders();

        when(properties.qrKey()).thenReturn("qr-key");
        when(properties.qrSecret()).thenReturn("qr-secret");

        when(signatureService.hmacSHA256(anyString(), anyString())).thenReturn("nspk_signature_hash");

        Consumer<HttpHeaders> headersConsumer = service.headers(request, "{}");
        headersConsumer.accept(headers);

        verify(signatureService).hmacSHA256(textCaptor.capture(), secretCaptor.capture());

        String actualTextPassedToHash = textCaptor.getValue();
        String actualSecretPassedToHash = secretCaptor.getValue();

        assertEquals("qr-secret", actualSecretPassedToHash);
        assertTrue(actualTextPassedToHash.startsWith("qr-key"));

        assertNotNull(headers.getFirst("NONCE"));
        assertEquals("application/json", headers.getFirst("Content-Type"));
        assertEquals("qr-key", headers.getFirst("APIKEY"));
        assertEquals("NSPK_SIGNATURE_HASH", headers.getFirst("SIGNATURE"));
    }

    @Test
    void testHeaders_ShouldUseTPayKeysAndSecret_WhenMethodIsTPay() {
        DetailsRequest request = new DetailsRequest();
        request.setCurrentMerchantMethod(Method.T_PAY.name());
        HttpHeaders headers = new HttpHeaders();

        when(properties.tPayKey()).thenReturn("t-pay-key");
        when(properties.tPaySecret()).thenReturn("t-pay-secret");

        when(signatureService.hmacSHA256(anyString(), anyString())).thenReturn("tpay_signature_hash");

        Consumer<HttpHeaders> headersConsumer = service.headers(request, "{}");
        headersConsumer.accept(headers);

        verify(signatureService).hmacSHA256(textCaptor.capture(), secretCaptor.capture());

        String actualTextPassedToHash = textCaptor.getValue();
        String actualSecretPassedToHash = secretCaptor.getValue();

        assertEquals("t-pay-secret", actualSecretPassedToHash);
        assertTrue(actualTextPassedToHash.startsWith("t-pay-key"));

        assertNotNull(headers.getFirst("NONCE"));
        assertEquals("application/json", headers.getFirst("Content-Type"));
        assertEquals("t-pay-key", headers.getFirst("APIKEY"));
        assertEquals("TPAY_SIGNATURE_HASH", headers.getFirst("SIGNATURE"));
    }

    @Test
    void testBody_ShouldMapFieldsCorrectly_ForDefaultMethod() {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(250);
        detailsRequest.setCurrentMerchantMethod(Method.CARD.name());

        when(properties.shopId()).thenReturn("shop-id");

        Request resultBody = service.body(detailsRequest);

        assertNotNull(resultBody);
        assertEquals("250", resultBody.getAmount());
        assertEquals("shop-id", resultBody.getShop());
        assertEquals(Method.CARD, resultBody.getPaymentType());
        assertEquals(Method.CARD.getBankCode(), resultBody.getBank());

        assertNotNull(resultBody.getOperationId());
        assertDoesNotThrow(() -> UUID.fromString(resultBody.getOperationId()));
    }

    @Test
    void testBody_ShouldUseQrShopId_WhenMethodIsNspk() {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(500);
        detailsRequest.setCurrentMerchantMethod(Method.NSPK.name());

        when(properties.qrShopId()).thenReturn("qr-shop-id");

        Request resultBody = service.body(detailsRequest);

        assertNotNull(resultBody);
        assertEquals("500", resultBody.getAmount());
        assertEquals("qr-shop-id", resultBody.getShop());
        assertEquals(Method.NSPK, resultBody.getPaymentType());
        assertEquals(Method.NSPK.getBankCode(), resultBody.getBank());

        assertNotNull(resultBody.getOperationId());
    }

    @Test
    void testBody_ShouldUseTPayShopId_WhenMethodIsTPay() {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(750);
        detailsRequest.setCurrentMerchantMethod(Method.T_PAY.name());

        when(properties.tPayShopId()).thenReturn("t-pay-shop-id");

        Request resultBody = service.body(detailsRequest);

        assertNotNull(resultBody);
        assertEquals("750", resultBody.getAmount());
        assertEquals("t-pay-shop-id", resultBody.getShop());
        assertEquals(Method.T_PAY, resultBody.getPaymentType());
        assertEquals(Method.T_PAY.getBankCode(), resultBody.getBank());

        assertNotNull(resultBody.getOperationId());
    }


    @Test
    void testBuildResponse_WithCardNumber_ShouldReturnCardDetails() {
        Response response = new Response();
        response.setId("tx-100");
        response.setStatus(Status.PAID);
        response.setCardNumber("5555********4444");

        Response.Bank bank = new Response.Bank();
        bank.setName("VisaBank");
        response.setBank(bank);

        Optional<DetailsResponse> result = service.buildResponse(response);

        assertTrue(result.isPresent());
        DetailsResponse details = result.get();
        assertEquals("tx-100", details.getMerchantOrderId());
        assertEquals("PAID", details.getMerchantOrderStatus());
        assertEquals("VisaBank 5555********4444", details.getDetails());
        assertNull(details.getQr());
        assertEquals(Merchant.BUCKS_PAY, details.getMerchant());
    }

    @Test
    void testBuildResponse_WithNSPK_ShouldReturnCardDetails() {
        Response response = new Response();
        response.setId("tx-100");
        response.setStatus(Status.PAID);
        response.setQrLink("https://qr.nspk.ru/");

        Response.Bank bank = new Response.Bank();
        bank.setName("VisaBank");
        response.setBank(bank);

        Optional<DetailsResponse> result = service.buildResponse(response);

        assertTrue(result.isPresent());
        DetailsResponse details = result.get();
        assertEquals("tx-100", details.getMerchantOrderId());
        assertEquals("PAID", details.getMerchantOrderStatus());
        assertEquals("https://qr.nspk.ru/", details.getQr());
        assertNull(details.getDetails());
        assertEquals(Merchant.BUCKS_PAY, details.getMerchant());
    }

    @Test
    void testBuildResponse_WithPhoneNumber_ShouldReturnPhoneDetails() {
        Response response = new Response();
        response.setId("tx-200");
        response.setStatus(Status.PAID);
        response.setPhoneNumber("+79991112233");

        Response.Bank bank = new Response.Bank();
        bank.setName("MobileBank");
        response.setBank(bank);

        Optional<DetailsResponse> result = service.buildResponse(response);

        assertTrue(result.isPresent());
        DetailsResponse details = result.get();
        assertEquals("MobileBank +79991112233", details.getDetails());
        assertNull(details.getQr());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testMakeCancelRequest_ShouldCallRequestServiceWithPost() {
        CancelOrderRequest cancelRequest = new CancelOrderRequest();
        cancelRequest.setOrderId("98765");
        cancelRequest.setMethod(Method.CARD.name());

        when(properties.key()).thenReturn("key");
        when(properties.secret()).thenReturn("secret");
        when(signatureService.hmacSHA256(anyString(), anyString())).thenReturn("mocked_signature");

        service.makeCancelRequest(cancelRequest);

        ArgumentCaptor<Function<UriBuilder, URI>> uriCaptor = ArgumentCaptor.forClass(Function.class);
        ArgumentCaptor<Consumer<HttpHeaders>> headersCaptor = ArgumentCaptor.forClass(Consumer.class);

        verify(requestService).request(
                eq(webClient),
                eq(HttpMethod.POST),
                uriCaptor.capture(),
                headersCaptor.capture(),
                isNull()
        );

        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        URI finalUri = uriCaptor.getValue().apply(uriBuilder);
        assertEquals("/invoice/98765/cancel", finalUri.getPath());

        HttpHeaders headers = new HttpHeaders();
        headersCaptor.getValue().accept(headers);
        assertEquals("key", headers.getFirst("APIKEY"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testMakeCancelRequest_ShouldCallRequestServiceWithPost_ForNspk() {
        CancelOrderRequest cancelRequest = new CancelOrderRequest();
        cancelRequest.setOrderId("98765");
        cancelRequest.setMethod(Method.NSPK.name());

        when(properties.qrKey()).thenReturn("qr-key");
        when(properties.qrSecret()).thenReturn("qr-secret");
        when(signatureService.hmacSHA256(anyString(), anyString())).thenReturn("mocked_nspk_signature");

        service.makeCancelRequest(cancelRequest);

        ArgumentCaptor<Function<UriBuilder, URI>> uriCaptor = ArgumentCaptor.forClass(Function.class);
        ArgumentCaptor<Consumer<HttpHeaders>> headersCaptor = ArgumentCaptor.forClass(Consumer.class);

        verify(requestService).request(
                eq(webClient),
                eq(HttpMethod.POST),
                uriCaptor.capture(),
                headersCaptor.capture(),
                isNull()
        );

        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        URI finalUri = uriCaptor.getValue().apply(uriBuilder);
        assertEquals("/invoice/98765/cancel", finalUri.getPath());

        HttpHeaders headers = new HttpHeaders();
        headersCaptor.getValue().accept(headers);

        verify(signatureService).hmacSHA256(textCaptor.capture(), secretCaptor.capture());

        assertEquals("qr-secret", secretCaptor.getValue());
        assertTrue(textCaptor.getValue().startsWith("qr-key"));

        assertEquals("qr-key", headers.getFirst("APIKEY"));
        assertEquals("MOCKED_NSPK_SIGNATURE", headers.getFirst("SIGNATURE"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testMakeCancelRequest_ShouldCallRequestServiceWithPost_ForTPay() {
        CancelOrderRequest cancelRequest = new CancelOrderRequest();
        cancelRequest.setOrderId("55555");
        cancelRequest.setMethod(Method.T_PAY.name());

        when(properties.tPayKey()).thenReturn("t-pay-key");
        when(properties.tPaySecret()).thenReturn("t-pay-secret");
        when(signatureService.hmacSHA256(anyString(), anyString())).thenReturn("mocked_tpay_signature");

        service.makeCancelRequest(cancelRequest);

        ArgumentCaptor<Function<UriBuilder, URI>> uriCaptor = ArgumentCaptor.forClass(Function.class);
        ArgumentCaptor<Consumer<HttpHeaders>> headersCaptor = ArgumentCaptor.forClass(Consumer.class);

        verify(requestService).request(
                eq(webClient),
                eq(HttpMethod.POST),
                uriCaptor.capture(),
                headersCaptor.capture(),
                isNull()
        );

        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        URI finalUri = uriCaptor.getValue().apply(uriBuilder);
        assertEquals("/invoice/55555/cancel", finalUri.getPath());

        HttpHeaders headers = new HttpHeaders();
        headersCaptor.getValue().accept(headers);

        verify(signatureService).hmacSHA256(textCaptor.capture(), secretCaptor.capture());

        assertEquals("t-pay-secret", secretCaptor.getValue());
        assertTrue(textCaptor.getValue().startsWith("t-pay-key"));

        assertEquals("t-pay-key", headers.getFirst("APIKEY"));
        assertEquals("MOCKED_TPAY_SIGNATURE", headers.getFirst("SIGNATURE"));
    }

    @Test
    void getMerchantShouldReturnPrismaPay() {
        assertEquals(Merchant.BUCKS_PAY, service.getMerchant());
    }

}
