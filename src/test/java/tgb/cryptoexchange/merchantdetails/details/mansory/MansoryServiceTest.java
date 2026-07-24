package tgb.cryptoexchange.merchantdetails.details.mansory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.CancelOrderRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.MansoryImplProperties;
import tgb.cryptoexchange.merchantdetails.service.RequestService;

import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MansoryServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private MansoryImplProperties mansoryProperties;

    @Mock
    private CallbackConfig callbackConfig;

    @Mock
    private RequestService requestService;

    private MansoryImplService service;

    @BeforeEach
    void setUp() {
        service = new MansoryImplService(webClient, mansoryProperties, callbackConfig);
        ReflectionTestUtils.setField(service, "requestService", requestService);
    }

    @Test
    @DisplayName("getMerchant должен возвращать Merchant.MANSORY")
    void shouldReturnCorrectMerchant() {
        assertThat(service.getMerchant()).isEqualTo(Merchant.MANSORY);
    }

    @Test
    @DisplayName("uriBuilder должен формировать путь /merchant/payment")
    void shouldBuildCorrectUri() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();

        Function<UriBuilder, URI> action = service.uriBuilder(new DetailsRequest());
        URI result = action.apply(uriBuilder);

        assertThat(result.getPath()).isEqualTo("/merchant/payment");
    }

    @Test
    @DisplayName("headers должен добавлять API-ключи из MansoryImplProperties")
    void shouldAddRequiredHeaders() {
        when(mansoryProperties.apiKey()).thenReturn("impl-api-key");
        when(mansoryProperties.secret()).thenReturn("impl-secret-key");
        HttpHeaders headers = new HttpHeaders();

        Consumer<HttpHeaders> headersConsumer = service.headers(new DetailsRequest(), "body-stub");
        headersConsumer.accept(headers);

        assertThat(headers.getFirst("Content-Type")).isEqualTo("application/json");
        assertThat(headers.getFirst("X-API-Key")).isEqualTo("impl-api-key");
        assertThat(headers.getFirst("X-Secret-Key")).isEqualTo("impl-secret-key");
    }

    @Test
    @DisplayName("body должен собирать объект Request с правильным callback URL для MANSORY")
    void shouldCreateValidRequestBody() {
        DetailsRequest request = new DetailsRequest();
        request.setAmount(2500);
        request.setCurrentMerchantMethod(Method.CARD.name());

        when(callbackConfig.getGatewayUrl()).thenReturn("https://gateway.ru");
        when(callbackConfig.getCallbackSecret()).thenReturn("top-secret");

        Request result = service.body(request);

        assertThat(result.getAmount()).isEqualTo(2500);
        assertThat(result.getMethod()).isEqualTo(Method.CARD);
        assertThat(result.getCallbackUrl())
                .isEqualTo("https://gateway.ru/merchant-details/callback?merchant=MANSORY&secret=top-secret");
    }


    @Test
    @DisplayName("buildResponse должен брать cardNumber, если он передан")
    void shouldMapResponseWithCardNumber() {
        Response response = createBaseResponse();
        response.getRequisites().setBankName("Alfa-Bank");
        response.getRequisites().setCardNumber("1111 2222 3333 4444");
        Optional<DetailsResponse> resultOpt = service.buildResponse(response);

        assertThat(resultOpt).isPresent();
        DetailsResponse result = resultOpt.get();
        assertThat(result.getMerchant()).isEqualTo(Merchant.MANSORY);
        assertThat(result.getDetails()).isEqualTo("Alfa-Bank 1111 2222 3333 4444");
        assertThat(result.getMerchantOrderStatus()).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("buildResponse должен брать phone, если он передан")
    void shouldMapResponseWithPhone() {
        Response response = createBaseResponse();
        response.getRequisites().setBankName("Alfa-Bank");
        response.getRequisites().setCardNumber("88005553535");
        Optional<DetailsResponse> resultOpt = service.buildResponse(response);

        assertThat(resultOpt).isPresent();
        DetailsResponse result = resultOpt.get();
        assertThat(result.getMerchant()).isEqualTo(Merchant.MANSORY);
        assertThat(result.getDetails()).isEqualTo("Alfa-Bank 88005553535");
        assertThat(result.getMerchantOrderStatus()).isEqualTo("COMPLETED");
    }

    private Response createBaseResponse() {
        Response response = new Response();
        response.setOrderId("id-100");
        response.setAmount(1000);
        response.setStatus(Status.COMPLETED);
        response.setRequisites(new Response.Requisites());
        return response;
    }

    @Test
    @DisplayName("makeCancelRequest должен вызывать requestService с правильным HTTP методом")
    void shouldExecuteCancelPostRequest() {
        CancelOrderRequest cancelRequest = new CancelOrderRequest();
        cancelRequest.setOrderId("order-to-cancel");

        service.makeCancelRequest(cancelRequest);
        verify(requestService).request(
                eq(webClient),
                eq(HttpMethod.POST),
                any(),
                any(),
                isNull()
        );
    }
}
