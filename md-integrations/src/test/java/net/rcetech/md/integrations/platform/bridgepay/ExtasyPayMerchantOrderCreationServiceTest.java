package net.rcetech.md.integrations.platform.bridgepay;

import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.integrations.properties.ExtasyPayProperties;
import net.rcetech.md.integrations.service.RequestService;
import net.rcetech.md.integrations.service.SignatureService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExtasyPayMerchantOrderCreationServiceTest {

    @Mock
    private ExtasyPayProperties extasyPayProperties;

    @Mock
    private SignatureService signatureService;

    @Mock
    private CallbackConfig callbackConfig;

    @Mock
    private RequestService requestService;

    @Mock
    private RestClient restClient;

    @InjectMocks
    private ExtasyPayMerchantOrderCreationService extasyPayMerchantOrderCreationService;

    @Test
    void getMerchantShouldReturnExtasyPay() {
        assertEquals(Merchant.EXTASY_PAY, extasyPayMerchantOrderCreationService.getMerchant());
    }

    @Test
    void uriBuilderShouldSetPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/api/merchant/invoices",
                extasyPayMerchantOrderCreationService.uriBuilder(null).apply(uriBuilder).getPath());
    }

    @CsvSource({
            "https://extasy-pay.com,HiubKWrJW8ytPGRR0E4XficeZ0ChxXHf,4u7XPRW4GzPlL187,sign1",
            "https://extasy-pay.merch.info,18td2niwHwNa2SOy3jhaQdWQrZeFgvy6,oNnM2Ud8RGA8FjCc,someSign2"
    })
    @ParameterizedTest
    void headersShouldSetRequiredHeaders(String url, String secret, String key, String sign)
            throws Exception {
        when(extasyPayProperties.url()).thenReturn(url);
        when(extasyPayProperties.key()).thenReturn(key);
        when(extasyPayProperties.secret()).thenReturn(secret);
        String expectedBody = "{\"field\": \"expectedBody\"}";
        ArgumentCaptor<String> dataArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> secretArgumentCaptor = ArgumentCaptor.forClass(String.class);
        when(signatureService.hmacSHA1(dataArgumentCaptor.capture(), secretArgumentCaptor.capture())).thenReturn(sign);

        HttpHeaders headers = new HttpHeaders();
        OrderCreationRequest request = new OrderCreationRequest();
        request.setMethod(Method.TO_CARD.name());
        extasyPayMerchantOrderCreationService.headers(request, expectedBody).accept(headers);
        assertAll(
                () -> assertEquals("application/json", Objects.requireNonNull(headers.get("Content-Type")).getFirst()),
                () -> assertEquals(key, Objects.requireNonNull(headers.get("X-Identity")).getFirst()),
                () -> assertEquals(sign, Objects.requireNonNull(headers.get("X-Signature")).getFirst()),
                () -> assertEquals("POST" + url + "/api/merchant/invoices" + expectedBody, dataArgumentCaptor.getValue()),
                () -> assertEquals(secret, secretArgumentCaptor.getValue())
        );
    }

    @Test
    void bodyShouldBuildRequestObject() {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setAmount(1000);
        detailsRequest.setMethod(Method.SBP.name());
        when(callbackConfig.getGatewayUrl()).thenReturn("https://gateway.example.com");
        when(callbackConfig.getCallbackSecret()).thenReturn("secret");
        when(extasyPayProperties.token()).thenReturn("token");

        Request actual = extasyPayMerchantOrderCreationService.body(detailsRequest);

        assertAll(
                () -> assertEquals("1000", actual.getAmount()),
                () -> assertEquals("RUB", actual.getCurrency()),
                () -> assertEquals("https://gateway.example.com/merchant-details/callback?merchant=EXTASY_PAY&secret=secret",
                        actual.getNotificationUrl()),
                () -> assertEquals("token", actual.getNotificationToken()),
                () -> assertDoesNotThrow(() -> UUID.fromString(actual.getInternalId())),
                () -> assertEquals(Method.SBP, actual.getPaymentOption()),
                () -> assertTrue(actual.getStartDeal())
        );
    }

    @Test
    void buildResponseShouldReturnMappedRequisiteDTO() {
        Response response = new Response();
        DealDTO dealDTO = new DealDTO();
        dealDTO.setPaymentMethod(Bank.SBERBANK.getValue());
        dealDTO.setPaymentOption(Method.TO_CARD);
        RequisitesDTO requisitesDTO = new RequisitesDTO();
        requisitesDTO.setRequisites("4444 4444 4444 4444");
        dealDTO.setRequisites(requisitesDTO);
        response.setDeals(List.of(dealDTO));
        response.setId("2ac093d0-49aa-4bc7-a7dc-61fa62820544");

        Optional<DetailsResponse> actual = extasyPayMerchantOrderCreationService.buildResponse(response);

        assertTrue(actual.isPresent());
        DetailsResponse actualResponse = actual.get();
        assertAll(
                () -> assertEquals(Merchant.EXTASY_PAY, actualResponse.getMerchant()),
                () -> assertEquals("2ac093d0-49aa-4bc7-a7dc-61fa62820544", actualResponse.getMerchantOrderId()),
                () -> assertEquals(Status.NEW.name(), actualResponse.getMerchantOrderStatus()),
                () -> assertEquals("4444 4444 4444 4444", actualResponse.getDetails()),
                () -> assertEquals(Bank.SBERBANK.getDisplayName(), actualResponse.getBank()),
                () -> assertEquals("TO_CARD", actualResponse.getPaymentMethod())
        );
    }

    @Test
    void buildResponseShouldSetQrForSbpQrDeal() {
        Response response = new Response();
        DealDTO dealDTO = new DealDTO();
        dealDTO.setPaymentOption(Method.SBP_QR);
        dealDTO.setQrCodeLink("https://www.sberbank.com/sms/pbpn?requisiteNumber=4444444444444444");
        response.setDeals(List.of(dealDTO));
        response.setId("9e6eee50-dcbb-47be-85a5-494db6d49157");

        Optional<DetailsResponse> actual = extasyPayMerchantOrderCreationService.buildResponse(response);

        assertTrue(actual.isPresent());
        DetailsResponse actualResponse = actual.get();
        assertAll(
                () -> assertEquals(
                        "https://www.sberbank.com/sms/pbpn?requisiteNumber=4444444444444444", actualResponse.getQr()),
                () -> assertEquals("SBP_QR", actualResponse.getPaymentMethod())
        );
    }

    @Test
    void getCallbackTypeShouldReturnBridgeCallback() {
        assertEquals(BridgeCallback.class, extasyPayMerchantOrderCreationService.getCallbackType());
    }
}
