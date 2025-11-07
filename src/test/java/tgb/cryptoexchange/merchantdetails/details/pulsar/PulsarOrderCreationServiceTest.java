package tgb.cryptoexchange.merchantdetails.details.pulsar;

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
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.PulsarProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PulsarOrderCreationServiceTest {

    @Mock
    private SignatureService signatureService;

    @Mock
    private PulsarProperties pulsarProperties;

    @InjectMocks
    private PulsarOrderCreationService pulsarOrderCreationService;

    @Test
    void getMerchantShouldReturnPulsar() {
        assertEquals(Merchant.PULSAR, pulsarOrderCreationService.getMerchant());
    }

    @Test
    void uriBuilderShouldAddPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals(
                "/api/v2/payments",
                pulsarOrderCreationService.uriBuilder(null).apply(uriBuilder).getPath()
        );
    }

    @CsvSource(textBlock = """
            J2fetkXmS5Db8KC,tvwdeIac8GnlYbljSIEVJpJ3NMcBmTyB,\
            1b2c16b75bd2a870c114153ccda5bcfca63314bc722fa160d690de133ccbb9db,\
            {"amount":5440,"merchantId":"BTCMONEY24","orderId":"255523","method":"c2c","userId":"2515635235"}
            oFHjD3gk7RY4Lks,3NGEq3Oxh9b4furwcJ8q4EzLFblexI9g,\
            d245ec4403b44aaeefb7010470bed7ff38b746784c61550c8693815ab0c32eb6,\
            {"amount":12200,"merchantId":"bulbabot","orderId":"2ew555asd235qwe3","method":"sbp","userId":"123414551"}
            """)
    @ParameterizedTest
    void headersShouldAddRequiredHeaders(String token, String secret, String signature, String body) {
        when(pulsarProperties.token()).thenReturn(token);
        when(pulsarProperties.secret()).thenReturn(secret);
        when(signatureService.hmacSHA256(anyString(), anyString())).thenReturn(signature);

        HttpHeaders headers = new HttpHeaders();
        pulsarOrderCreationService.headers(null, body).accept(headers);
        assertAll(
                () -> assertEquals("Bearer " + token, headers.getFirst("Authorization")),
                () -> assertEquals("application/json", headers.getFirst("Content-Type")),
                () -> assertEquals(signature, headers.getFirst("Signature"))
        );
    }

    @CsvSource(textBlock = """
            5442,CARD,255525050,nHaqQ4QdTL
            2550,SBP,122551525,XqqtxmPQ22
            """)
    @ParameterizedTest
    void bodyShouldBuildRequestObject(Integer amount, Method method, Long chatId, String code) {
        when(pulsarProperties.code()).thenReturn(code);
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setMethod(method.name());
        detailsRequest.setChatId(chatId);

        Request actual = pulsarOrderCreationService.body(detailsRequest);

        assertAll(
                () -> assertEquals(amount, actual.getAmount()),
                () -> assertEquals(code, actual.getMerchantId()),
                () -> assertEquals(method, actual.getMethod()),
                () -> assertDoesNotThrow(() -> UUID.fromString(actual.getOrderId())),
                () -> assertEquals(chatId.toString(), actual.getUserId())
        );
    }

    @CsvSource(textBlock = """
            Alfa,74292492494,e05dc367-8ad3-4548-ada0-f0514b10c75b,CREATED
            T-Bank,1234123412341234,6b4f44ed-a398-454f-b78c-e45d7fd2e968,PENDING
            """)
    @ParameterizedTest
    void buildResponseShouldBuildResponseObject(String bankName, String address, String id, Status status) {
        Response response = new Response();
        Response.Result result = new Response.Result();
        result.setId(id);
        result.setAddress(address);
        result.setBankName(bankName);
        result.setState(status);
        response.setResult(result);

        Optional<DetailsResponse> maybeResponse = pulsarOrderCreationService.buildResponse(response);
        assertTrue(maybeResponse.isPresent());
        DetailsResponse actual = maybeResponse.get();
        assertAll(
                () -> assertEquals(bankName + " " + address, actual.getDetails()),
                () -> assertEquals(Merchant.PULSAR, actual.getMerchant()),
                () -> assertEquals(id, actual.getMerchantOrderId()),
                () -> assertEquals(status.name(), actual.getMerchantOrderStatus())
        );
    }
}