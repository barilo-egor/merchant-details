package tgb.cryptoexchange.merchantdetails.details.asgard;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.AsgardImplProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsgardImplOrderCreationServiceTest {

    @Mock
    private AsgardImplProperties asgardProperties;
    @Mock
    private SignatureService signatureService;
    @Mock
    private CallbackConfig callbackConfig;

    @InjectMocks
    private AsgardImplOrderCreationService service;

    @Test
    void uriBuilder_ShouldReturnCorrectPath() {
        DetailsRequest request = new DetailsRequest();
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();

        Function<UriBuilder, URI> resultFunc = service.uriBuilder(request);
        URI uri = resultFunc.apply(uriBuilder);

        assertEquals("/payments", uri.getPath());
    }

    @Test
    void headers_ShouldAddRequiredHeaders() {
        String body = "{\"test\":\"json\"}";
        String token = "test-token";
        String secret = "test-secret";
        String signature = "test-signature";

        when(asgardProperties.token()).thenReturn(token);
        when(asgardProperties.secret()).thenReturn(secret);
        when(signatureService.hmacSHA256(body, secret)).thenReturn(signature);

        HttpHeaders headers = new HttpHeaders();

        Consumer<HttpHeaders> headersConsumer = service.headers(new DetailsRequest(), body);
        headersConsumer.accept(headers);

        assertEquals("application/json", headers.getFirst("Content-Type"));
        assertEquals("Bearer " + token, headers.getFirst("Authorization"));
        assertEquals(signature, headers.getFirst("Signature"));
    }

    @Test
    void body_ShouldMapRequestCorrectly() {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(5936);
        detailsRequest.setCurrentMerchantMethod("CARD");

        when(asgardProperties.merchantId()).thenReturn("M-123");
        when(callbackConfig.getGatewayUrl()).thenReturn("https://test.com");
        when(callbackConfig.getCallbackSecret()).thenReturn("cb-secret");

        Request result = service.body(detailsRequest);

        assertNotNull(result.getOrderId());
        assertEquals("M-123", result.getMerchantId());
        assertEquals(5936, result.getAmount().intValue());
        assertTrue(result.getCallbackUri().contains("merchant=ASGARD"));
        assertTrue(result.getCallbackUri().contains("secret=cb-secret"));
    }

    @Test
    void buildResponse_ShouldMapProperties() {
        Response response = new Response();
        Response.Requisites requisites = new Response.Requisites();
        requisites.setId("order-1");
        requisites.setState(Status.CREATED);
        requisites.setMethod(Method.CARD);
        requisites.setBankName("Sber");
        requisites.setAmount(5000.0);
        requisites.setAddress("1234 56xx");
        response.setRequisites(requisites);

        Optional<DetailsResponse> result = service.buildResponse(response);

        assertTrue(result.isPresent());
        DetailsResponse dr = result.get();
        assertEquals("order-1", dr.getMerchantOrderId());
        assertEquals("CREATED", dr.getMerchantOrderStatus());
        assertEquals("Sber 1234 56xx", dr.getDetails());
        assertEquals("CARD", dr.getPaymentMethod());
    }


    @Test
    void getMerchantShouldReturnGambit() {
        assertEquals(Merchant.ASGARD, service.getMerchant());
    }

}