package tgb.cryptoexchange.merchantdetails.details.eclipsegate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.EclipseGateProperties;
import tgb.cryptoexchange.merchantdetails.service.RequestService;

import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EclipseGateServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private EclipseGateProperties eclipseGateProperties;

    @Mock
    private CallbackConfig callbackConfig;

    @Mock
    private RequestService requestService;

    private EclipseGateImplService service;

    @BeforeEach
    void setUp() {
        service = new EclipseGateImplService(webClient, eclipseGateProperties, callbackConfig);
        ReflectionTestUtils.setField(service, "requestService", requestService);
    }

    @Test
    @DisplayName("getMerchant должен возвращать Merchant.ECLIPSE_GATE")
    void shouldReturnCorrectMerchant() {
        assertThat(service.getMerchant()).isEqualTo(Merchant.ECLIPSE_GATE);
    }

    @Test
    @DisplayName("uriBuilder должен формировать путь /orders")
    void shouldBuildCorrectUri() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();

        Function<UriBuilder, URI> action = service.uriBuilder(new DetailsRequest());
        URI result = action.apply(uriBuilder);

        assertThat(result.getPath()).isEqualTo("/orders");
    }

    @Test
    @DisplayName("headers должен добавлять API-ключи из EclipseGateProperties")
    void shouldAddRequiredHeaders() {
        when(eclipseGateProperties.apiKey("SPB")).thenReturn("spb-api-key");
        HttpHeaders headers = new HttpHeaders();

        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setCurrentMerchantMethod("SPB");
        Consumer<HttpHeaders> headersConsumer = service.headers(detailsRequest, "body-stub");
        headersConsumer.accept(headers);

        assertThat(headers.getFirst("Content-Type")).isEqualTo("application/json");
        assertThat(headers.getFirst("api-key")).isEqualTo("spb-api-key");
    }

    @Test
    @DisplayName("body должен собирать объект Request с правильным callback URL для ECLIPSE_GATE")
    void shouldCreateValidRequestBody() {
        DetailsRequest request = new DetailsRequest();
        request.setCurrentMerchantMethod(Method.CARD.name());

        when(callbackConfig.getGatewayUrl()).thenReturn("https://gateway.ru");
        when(callbackConfig.getCallbackSecret()).thenReturn("top-secret");

        Request result = service.body(request);

        assertThat(result.getMethod()).isEqualTo(Method.CARD);
        assertThat(result.getCallbackUrl())
                .isEqualTo("https://gateway.ru/merchant-details/callback?merchant=ECLIPSE_GATE&secret=top-secret");
    }


    @Test
    @DisplayName("buildResponse должен брать bill, если он передан")
    void shouldMapResponseWithBill() {
        Response response = createBaseResponse();
        response.getRequisites().setBankName("Alfa-Bank");
        response.getRequisites().setBill("1111 2222 3333 4444");
        Optional<DetailsResponse> resultOpt = service.buildResponse(response);

        assertThat(resultOpt).isPresent();
        DetailsResponse result = resultOpt.get();
        assertThat(result.getMerchant()).isEqualTo(Merchant.ECLIPSE_GATE);
        assertThat(result.getDetails()).isEqualTo("Alfa-Bank 1111 2222 3333 4444");
        assertThat(result.getMerchantOrderStatus()).isEqualTo("PENDING");
    }

    private Response createBaseResponse() {
        Response response = new Response();
        response.setOrderId("id-100");
        response.setAmount(1000);
        response.setStatus(Status.PENDING);
        response.setRequisites(new Response.Requisites());
        return response;
    }

}

