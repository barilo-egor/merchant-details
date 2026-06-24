package tgb.cryptoexchange.merchantdetails.details.paysync;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.PaySyncProperties;

import java.net.URI;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaySyncOrderCreationServiceTest {

    @Mock
    private PaySyncProperties paySyncProperties;

    @InjectMocks
    private PaySyncOrderCreationService service;

    @CsvSource({
            "JQX1BI3Vs36UnMB",
            "y701U9erXYfOAdX",
            "w1vGjx4COVk531JZgj6dsh7uT"
    })
    @ParameterizedTest
    void shouldCorrectlyConfigureUriBuilder(String clientId) {
        when(paySyncProperties.clientId()).thenReturn(clientId);
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(1500);

        UriBuilder realUriBuilder = new DefaultUriBuilderFactory("https://paysync.bot").builder();
        Function<UriBuilder, URI> uriFunction = service.uriBuilder(detailsRequest);
        URI resultUri = uriFunction.apply(realUriBuilder);
        String uriString = resultUri.toString();

        assertTrue(uriString.contains("/api/client" + paySyncProperties.clientId()));
        assertTrue(uriString.contains("/amount1500"));
        assertTrue(uriString.contains("/currencyRUB"));
    }

    @Test
    void shouldReturnNullForBody() {
        assertNull(service.body(new DetailsRequest()));
    }

    @Test
    void shouldReturnCorrectHttpMethodAndMerchant() {
        assertEquals(HttpMethod.GET, service.method());
        assertEquals(Merchant.PAYSYNC, service.getMerchant());
    }

    @Test
    void shouldSuccessfullyBuildResponse() {
        Response mockResponse = new Response();
        mockResponse.setId("order-uuid-999");
        mockResponse.setBank("Sberbank");
        mockResponse.setCardNumber("4444 4444 4444 4444");
        mockResponse.setStatus(Status.PAID);

        Optional<DetailsResponse> resultOpt = service.buildResponse(mockResponse);

        assertTrue(resultOpt.isPresent());
        DetailsResponse response = resultOpt.get();
        assertEquals(Merchant.PAYSYNC, response.getMerchant());
        assertEquals("order-uuid-999", response.getMerchantOrderId());
        assertEquals("PAID", response.getMerchantOrderStatus());
        assertEquals("Sberbank 4444 4444 4444 4444", response.getDetails());
    }
}
