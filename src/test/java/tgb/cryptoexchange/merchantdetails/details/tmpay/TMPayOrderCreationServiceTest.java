package tgb.cryptoexchange.merchantdetails.details.tmpay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.TMPayProperties;

import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TMPayOrderCreationServiceTest {

    @Mock
    private TMPayProperties tmPayProperties;

    @Mock
    private CallbackConfig callbackConfig;

    @InjectMocks
    private TMPayOrderCreationService tmPayOrderCreationService;

    @Test
    void shouldCorrectlyConfigureUriBuilder() {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(1500);
        detailsRequest.setCurrentMerchantMethod(Method.CARD.name());

        UriBuilder realUriBuilder = new DefaultUriBuilderFactory("https://tmpay.xyz/api/v2").builder();
        Function<UriBuilder, URI> uriFunction = tmPayOrderCreationService.uriBuilder(detailsRequest);
        URI resultUri = uriFunction.apply(realUriBuilder);

        String uriString = resultUri.toString();
        assertTrue(uriString.contains("/invoice/create"));
        assertTrue(uriString.contains("amount=1500"));
        assertTrue(uriString.contains("invoiceType=Card"));
        assertTrue(uriString.contains("callbackUrl"));
    }

    @Test
    void shouldCorrectlyAddHeaders() {
        String expectedApiKey = "test-api-key-123";
        when(tmPayProperties.key()).thenReturn(expectedApiKey);

        DetailsRequest detailsRequest = new DetailsRequest();
        HttpHeaders headers = new HttpHeaders();

        Consumer<HttpHeaders> headersConsumer = tmPayOrderCreationService.headers(detailsRequest, null);
        headersConsumer.accept(headers);

        assertEquals(expectedApiKey, headers.getFirst("apikey"));
    }

    @Test
    void shouldReturnNullForBody() {
        assertNull(tmPayOrderCreationService.body(new DetailsRequest()));
    }

    @Test
    void shouldReturnCorrectHttpMethodAndMerchant() {
        assertEquals(HttpMethod.GET, tmPayOrderCreationService.method());
        assertEquals(Merchant.TM_PAY, tmPayOrderCreationService.getMerchant());
    }

    @Test
    void shouldSuccessfullyBuildResponse() {
        Response mockResponse = new Response();
        Response.Data mockData = new Response.Data();
        mockData.setUuid("order-uuid-999");
        mockData.setBank("Sberbank");
        mockData.setCard("4444 4444 4444 4444");
        mockResponse.setData(mockData);

        Optional<DetailsResponse> resultOpt = tmPayOrderCreationService.buildResponse(mockResponse);

        assertTrue(resultOpt.isPresent());
        DetailsResponse response = resultOpt.get();
        assertEquals(Merchant.TM_PAY, response.getMerchant());
        assertEquals("order-uuid-999", response.getMerchantOrderId());
        assertEquals("CREATED", response.getMerchantOrderStatus());
        assertEquals("Sberbank 4444 4444 4444 4444", response.getDetails());
    }
}
