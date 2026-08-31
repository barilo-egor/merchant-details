package tgb.cryptoexchange.merchantdetails.details.cube;

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
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.OrderCreationRequest;
import tgb.cryptoexchange.merchantdetails.properties.CubePropertiesImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CubeMerchantServiceTest {

    @Mock
    private CubePropertiesImpl cubeProperties;

    @InjectMocks
    private CubeImplOrderCreationService cubeService;

    @Mock
    private CallbackConfig callbackConfig;

    @Test
    void getMerchantShouldReturnPayLee() {
        assertEquals(Merchant.CUBE, cubeService.getMerchant());
    }

    @Test
    void uriBuilderShouldAddPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/transactions/payin", cubeService.uriBuilder(null).apply(uriBuilder).getPath());
    }

    @ParameterizedTest
    @CsvSource({
            "PzeiDYNh1RTRD5d, L76sF2r7uL1ClNF",
            "PzeiDYNh1rqwfRTRD5d, L76sF2r7ufaL1ClNF"
    })
    void headersShouldAddRequiredHeaders(String key, String privateKey) {
        HttpHeaders headers = new HttpHeaders();
        when(cubeProperties.key()).thenReturn(key);
        when(cubeProperties.privateKey()).thenReturn(privateKey);
        cubeService.headers(null, null).accept(headers);
        assertAll(
                () -> assertEquals(privateKey, headers.getFirst("Apiprivate")),
                () -> assertEquals(key, headers.getFirst("Apipublic")),
                () -> assertEquals("application/json", headers.getFirst(HttpHeaders.CONTENT_TYPE))
        );
    }

    @CsvSource(textBlock = """
            5600,CARD
            2504,SBP,
            2000,SIM,
            """)
    @ParameterizedTest
    void bodyShouldBuildRequestObject(Integer amount, Method method) {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setId("123456789");
        detailsRequest.setUserId("1231231231");
        detailsRequest.setMethod(method.name());
        Request actual = cubeService.body(detailsRequest);
        assertAll(
                () -> assertEquals(amount, Integer.valueOf(actual.getAmount())),
                () -> assertEquals(method, actual.getMethod()),
                () -> assertNotNull(actual.getExternalId()),
                () -> assertNotNull(actual.getCallbackUrl())
        );
    }

    @Test
    void bodyShouldGenerateUniqueExternalId() {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setAmount(1000);
        detailsRequest.setMethod("CARD");
        detailsRequest.setId("123");

        Request request1 = cubeService.body(detailsRequest);
        Request request2 = cubeService.body(detailsRequest);

        assertNotEquals(request1.getExternalId(), request2.getExternalId());
    }

    @Test
    void bodyShouldSetCallbackUrlCorrectly() {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setAmount(1000);
        detailsRequest.setMethod("CARD");
        detailsRequest.setId("123456");

        when(callbackConfig.getGatewayUrl()).thenReturn("https://api.example.com");
        when(callbackConfig.getCallbackSecret()).thenReturn("secret123");

        Request request = cubeService.body(detailsRequest);

        String expectedUrl = "https://api.example.com/merchant-details/callback/CUBE?dealId=123456&secret=secret123";
        assertEquals(expectedUrl, request.getCallbackUrl());
    }

    @Test
    void buildResponseShouldHandleDoubleAmount() {
        // Given
        Response response = new Response();
        Response.Data data = new Response.Data();
        data.setInternalId("INT-123456");
        data.setStatus(Status.APPEAL);
        data.setBankName("Test Bank");
        data.setReceiver("Test Receiver");
        data.setAmount(1999.99);
        response.setData(data);

        Optional<DetailsResponse> result = cubeService.buildResponse(response);

        assertTrue(result.isPresent());
        assertEquals(1999, result.get().getAmount());
    }
}