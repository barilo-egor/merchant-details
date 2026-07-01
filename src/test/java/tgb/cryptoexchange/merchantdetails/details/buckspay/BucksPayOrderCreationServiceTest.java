package tgb.cryptoexchange.merchantdetails.details.buckspay;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

//    @Test
//    void testHeaders_ShouldAddRequiredHeadersWithSignature() {
//        DetailsRequest request = new DetailsRequest();
//        HttpHeaders headers = new HttpHeaders();
//
//        when(properties.key()).thenReturn("merchant-key-123");
//        when(properties.secret()).thenReturn("secret-789");
//        when(signatureService.hmacSHA256(anyString(), eq("secret-789"))).thenReturn("mocked_signature_hash");
//
//        Consumer<HttpHeaders> headersConsumer = service.headers(request, "{}");
//        headersConsumer.accept(headers);
//
//        assertNotNull(headers.getFirst("NONCE"));
//        assertEquals("application/json", headers.getFirst("Content-Type"));
//        assertEquals("merchant-key-123", headers.getFirst("APIKEY"));
//        assertEquals("MOCKED_SIGNATURE_HASH", headers.getFirst("SIGNATURE"));
//    }

    @Test
    void testBody_ShouldMapFieldsCorrectly() {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(250);
        detailsRequest.setCurrentMerchantMethod(Method.CARD.name());

        when(properties.shopId()).thenReturn("shop-id-99");

        Request resultBody = service.body(detailsRequest);

        assertNotNull(resultBody);
        assertEquals("250", resultBody.getAmount());
        assertEquals("shop-id-99", resultBody.getShop());
        assertNotNull(resultBody.getOperationId());
        assertDoesNotThrow(() -> UUID.fromString(resultBody.getOperationId()));
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

//    @Test
//    @SuppressWarnings("unchecked")
//    void testMakeCancelRequest_ShouldCallRequestServiceWithPost() {
//        CancelOrderRequest cancelRequest = new CancelOrderRequest();
//        cancelRequest.setOrderId("98765");
//
//        service.makeCancelRequest(cancelRequest);
//
//        ArgumentCaptor<Function<UriBuilder, URI>> uriCaptor = ArgumentCaptor.forClass(Function.class);
//
//        verify(requestService).request(
//                eq(webClient),
//                eq(HttpMethod.POST),
//                uriCaptor.capture(),
//                any(Consumer.class),
//                isNull()
//        );
//
//        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
//        URI finalUri = uriCaptor.getValue().apply(uriBuilder);
//        assertEquals("/invoice/98765/cancel", finalUri.getPath());
//    }

    @Test
    void getMerchantShouldReturnPrismaPay() {
        assertEquals(Merchant.BUCKS_PAY, service.getMerchant());
    }

}
