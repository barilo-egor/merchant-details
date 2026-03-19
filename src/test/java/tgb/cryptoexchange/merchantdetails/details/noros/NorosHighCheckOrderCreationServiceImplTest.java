package tgb.cryptoexchange.merchantdetails.details.noros;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.CancelOrderRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.NorosHighCheckProperties;
import tgb.cryptoexchange.merchantdetails.service.RequestService;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NorosHighCheckOrderCreationServiceImplTest {

    @Mock
    private NorosHighCheckProperties norosHighCheckProperties;

    @InjectMocks
    private NorosHighCheckOrderCreationServiceImpl norosHighCheckOrderCreationService;

    @Mock
    private WebClient webClient;

    @Mock
    private RequestService requestService;

    @Captor
    private ArgumentCaptor<Function<UriBuilder, URI>> uriBuilderCaptor;

    @BeforeEach
    void setUp() {
        lenient().when(norosHighCheckProperties.key()).thenReturn("test-api-key");
    }

    @Test
    void getMerchantShouldReturnNorosHighCheck() {
        assertEquals(Merchant.NOROS_HIGH_CHECK, norosHighCheckOrderCreationService.getMerchant());
    }

    @Test
    void uriBuilderShouldAddPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals(
                "/transaction",
                norosHighCheckOrderCreationService.uriBuilder(null).apply(uriBuilder).getPath()
        );
    }

    @Test
    void shouldAddCorrectHeaders() {
        HttpHeaders headers = new HttpHeaders();
        norosHighCheckOrderCreationService.headers(new DetailsRequest(), "").accept(headers);

        assertEquals("application/json", headers.getFirst(HttpHeaders.CONTENT_TYPE));
        assertEquals("test-api-key", headers.getFirst("api_key"));
    }

    @Test
    void shouldCorrectBuildBody() {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(1000);
        detailsRequest.setMethods(
                List.of(DetailsRequest.MerchantMethod.builder().merchant(Merchant.NOROS_HIGH_CHECK).method(
                        Method.SBP.name()).build()));
        Request resultBody = norosHighCheckOrderCreationService.body(detailsRequest);

        assertNotNull(resultBody.getOrderId());
        assertEquals(1000, resultBody.getAmount());
        assertEquals( Method.SBP, resultBody.getPaymentMethod());
    }

    @Test
    void shouldBuildResponseFromMerchantResponse() {
        Response merchantResponse = new Response();
        merchantResponse.setId("merchant-123");
        merchantResponse.setStatus(Status.OFFER);
        merchantResponse.setBankReceiver("Tinkoff");
        merchantResponse.setCard("22001100");
        merchantResponse.setAmount(1000);

        Optional<DetailsResponse> responseOpt = norosHighCheckOrderCreationService.buildResponse(merchantResponse);

        assertTrue(responseOpt.isPresent());
        DetailsResponse result = responseOpt.get();
        assertEquals("merchant-123", result.getMerchantOrderId());
        assertEquals("OFFER", result.getMerchantOrderStatus());
        assertEquals("Tinkoff 22001100", result.getDetails());
        assertEquals(1000, result.getAmount());
    }

    @CsvSource("""
            4be41169-2786-48f03-98b9-002a23417c45,CARD
            578f16e0-5941-430-80c3-b2d22ef302b8,SBP
            """)
    @ParameterizedTest
    void makeCancelRequestShouldMakeRequest(String orderId, Method method) {
        norosHighCheckOrderCreationService.setRequestService(requestService);
        CancelOrderRequest cancelOrderRequest = new CancelOrderRequest();
        cancelOrderRequest.setOrderId(orderId);
        cancelOrderRequest.setMethod(method.name());
        norosHighCheckOrderCreationService.makeCancelRequest(cancelOrderRequest);
        verify(requestService).request(eq(webClient), eq(HttpMethod.DELETE), uriBuilderCaptor.capture(),
                any(), eq(null));
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/transaction/" + orderId, uriBuilderCaptor.getValue().apply(uriBuilder).getPath());
    }

}