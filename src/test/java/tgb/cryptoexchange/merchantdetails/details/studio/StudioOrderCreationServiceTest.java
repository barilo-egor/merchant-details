package tgb.cryptoexchange.merchantdetails.details.studio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import tgb.cryptoexchange.merchantdetails.properties.StudioProperties;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StudioOrderCreationServiceTest {

    @Mock
    private CallbackConfig callbackConfig;

    @Mock
    private StudioProperties studioConfig;

    @Mock
    private DetailsRequest detailsRequest;

    @InjectMocks
    private StudioOrderCreationService service;

    @Test
    void uriBuilderShouldAddPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/api/v1/orders",
                service.uriBuilder(null).apply(uriBuilder).getPath());
    }

    @Test
    @DisplayName("Проверка заголовков для Card")
    void shouldAddCardHeaderWhenMethodIsCard() {
        when(studioConfig.getKey("CARD")).thenReturn("key-for-card");
        when(detailsRequest.getMerchantMethod(Merchant.STUDIO)).thenReturn(Optional.of("CARD"));
        HttpHeaders headers = new HttpHeaders();

        Consumer<HttpHeaders> consumer = service.headers(detailsRequest, "some body");
        consumer.accept(headers);

        assertEquals("application/json", headers.getFirst("Content-Type"));
        assertEquals("key-for-card", headers.getFirst("X-API-Key"));
    }

    @Test
    @DisplayName("Проверка заголовков для SBP")
    void shouldAddSbpHeaderWhenMethodIsSbp() {
        when(studioConfig.getKey("SBP")).thenReturn("key-for-sbp");
        when(detailsRequest.getMerchantMethod(Merchant.STUDIO)).thenReturn(Optional.of("SBP"));
        HttpHeaders headers = new HttpHeaders();

        service.headers(detailsRequest, "some body").accept(headers);

        assertEquals("key-for-sbp", headers.getFirst("X-API-Key"));
    }

    @ParameterizedTest
    @DisplayName("Создание тела запроса с проверкой реквизитов")
    @CsvSource(textBlock = """
            5000, CARD, orderId1,https://gateway.paysendmmm.online/, 47XeX7IStQnx2qD
            10500, SBP, orderId2,https://gateway.paysendmmm.online/, 53hhgfFFFh654
            2, SIM, orderId3,https://gateway.paysendmmm.online/, 47XeXew1Qnx2qD
            """)
    void body(Integer amount, Method mainMethod, String clientOrderId, String gatewayUrl, String secret) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setRequestId(clientOrderId);
        detailsRequest.setMethods(
                List.of(DetailsRequest.MerchantMethod.builder().merchant(Merchant.STUDIO).method(mainMethod.name())
                        .build()));
        when(callbackConfig.getCallbackSecret()).thenReturn(secret);
        when(callbackConfig.getGatewayUrl()).thenReturn(gatewayUrl);

        Request actual = service.body(detailsRequest);
        assertAll(
                () -> assertEquals(amount * 100, actual.getAmount()),
                () -> assertDoesNotThrow(() -> UUID.fromString(actual.getClientOrderId())),
                () -> assertEquals(gatewayUrl + "/merchant-details/callback?merchant=STUDIO&secret=" + secret,
                        actual.getCallbackUrl())
        );
    }

    @ParameterizedTest
    @DisplayName("Создание тела ответа с проверкой реквизитов")
    @CsvSource(textBlock = """
            1000, ID-444, PENDING, id234, Сбербанк, 2200111122223333
            500, ID-555, AWAITING_REQUISITES, id4444, Альфа-Банк, 4444555566667777
            """)
    void buildResponseShouldBuildResponseObject(Integer amount, String clientOrderId, Status status, String internalId,
            String bankName, String bik) {
        Response response = new Response();
        response.setAmount(amount);
        response.setClientOrderId(clientOrderId);
        response.setStatus(status);
        response.setInternalId(internalId);

        Response.Requisites requisite = new Response.Requisites();
        requisite.setBankName(bankName);
        requisite.setBik(bik);
        response.setRequisites(requisite);

        Optional<DetailsResponse> maybeResponse = service.buildResponse(response);

        assertTrue(maybeResponse.isPresent(), "Ответ должен присутствовать");
        DetailsResponse actual = maybeResponse.get();

        String expectedDetails = String.format("%s %s", bankName, bik);
        assertAll(
                () -> assertEquals(amount, actual.getAmount()),
                () -> assertEquals(internalId, actual.getMerchantOrderId()),
                () -> assertEquals(clientOrderId, actual.getRequestId()),
                () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(expectedDetails, actual.getDetails()),
                () -> assertEquals(service.getMerchant(), actual.getMerchant())
        );
    }

    @Test
    void getMerchantShouldReturnStudio() {
        assertEquals(Merchant.STUDIO, service.getMerchant());
    }

}

