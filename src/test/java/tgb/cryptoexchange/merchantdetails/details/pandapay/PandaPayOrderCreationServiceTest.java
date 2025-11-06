package tgb.cryptoexchange.merchantdetails.details.pandapay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.exception.SignatureCreationException;
import tgb.cryptoexchange.merchantdetails.properties.PandaPayProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PandaPayOrderCreationServiceTest {

    @Mock
    private PandaPayProperties pandaPayProperties;

    @Mock
    private SignatureService signatureService;

    @InjectMocks
    private PandaPayOrderCreationService pandaPayOrderCreationService;

    @Test
    void getMerchantShouldReturnPandaPay() {
        assertEquals(Merchant.PANDA_PAY, pandaPayOrderCreationService.getMerchant());
    }

    @Test
    void uriBuilderShouldAddPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/orders", pandaPayOrderCreationService.uriBuilder(null).apply(uriBuilder).getPath());
    }

    @CsvSource(value = {
            "'{\"amount_rub\":5042,\"requisite_type\":\"SBP\"}',sOxQqt8bvOYc77rSUqlrYbEfMj3CYgxT,92003059a722e7632fc06d79b2c682849aa17195b617580464d048e12242c844,ujYido2Ss6MUqFJ",
            "'{\"amount_rub\":2555,\"requisite_type\":\"card\"}',s5CRdZMIAShwDydq8Mf9BhxM8F3oUZK4,16ce4f4e1ad5442dcfd4488f02c7df2f6a55f130361506342ea7be3d4136b129,Sw1Zp3Z6JOE8CqN"
    })
    @ParameterizedTest
    void headersShouldAddRequiredHeaders(String body, String secret, String signature, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        when(pandaPayProperties.secret()).thenReturn(secret);
        when(pandaPayProperties.key()).thenReturn(key);
        when(signatureService.pandaPayHmacSHA256(anyString(), anyString())).thenReturn(signature);

        HttpHeaders headers = new HttpHeaders();
        pandaPayOrderCreationService.headers(null, body).accept(headers);
        ArgumentCaptor<String> stringToSignCaptor = ArgumentCaptor.forClass(String.class);
        verify(signatureService).pandaPayHmacSHA256(stringToSignCaptor.capture(), eq(secret));
        String actualStringToSign = stringToSignCaptor.getValue();

        assertAll(
                () -> assertTrue(actualStringToSign.endsWith(body)),
                () -> assertDoesNotThrow(() -> Long.parseLong(actualStringToSign.substring(0, actualStringToSign.indexOf(body)))),
                () -> assertEquals(key, Objects.requireNonNull(headers.get("X-API-Key")).getFirst()),
                () -> assertEquals(
                        actualStringToSign.substring(0, actualStringToSign.indexOf(body)),
                        Objects.requireNonNull(headers.get("X-Timestamp")).getFirst()
                ),
                () -> assertEquals(signature, Objects.requireNonNull(headers.get("X-Signature")).getFirst())
        );
    }

    @Test
    void headersShouldThrowSignatureCreationExceptionIfGenerateSignatureThrowsException() throws NoSuchAlgorithmException, InvalidKeyException {
        when(signatureService.pandaPayHmacSHA256(anyString(), anyString())).thenThrow(new NoSuchAlgorithmException());
        when(pandaPayProperties.secret()).thenReturn("secret");
        HttpHeaders headers = new HttpHeaders();
        Consumer<HttpHeaders> headersConsumer = pandaPayOrderCreationService.headers(null, "body");
        assertThrows(SignatureCreationException.class, () -> headersConsumer.accept(headers));
    }

    @CsvSource(textBlock = """
            12553,SBP
            2550,CARD
            """)
    @ParameterizedTest
    void bodyShouldBuildRequestObject(Integer amount, Method method) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setMethod(method.name());
        Request actual = pandaPayOrderCreationService.body(detailsRequest);
        assertAll(
                () -> assertEquals(amount, actual.getAmount()),
                () -> assertEquals(method, actual.getMethod())
        );
    }

    @CsvSource(textBlock = """
            ALFA bank,78884231464,PENDING,d9fb342a-10e3-4163-ad71-3be6912eb1cb
            Сбербанк,1234123412341234,COMPLETED,3b0ffa53-fb81-4c0f-8b22-47b687606ae3
            """)
    @ParameterizedTest
    void buildResponseShouldBuildResponseObject(String bank, String requisites, Status status, String uuid) {
        Response response = new Response();
        response.setUuid(uuid);
        response.setStatus(status);
        Response.RequisiteData requisiteData = new Response.RequisiteData();
        response.setRequisiteData(requisiteData);
        requisiteData.setBank(bank);
        requisiteData.setRequisites(requisites);
        Optional<DetailsResponse> maybeResponse = pandaPayOrderCreationService.buildResponse(response);
        assertTrue(maybeResponse.isPresent());
        DetailsResponse actual = maybeResponse.get();
        assertAll(
                () -> assertEquals(Merchant.PANDA_PAY, actual.getMerchant()),
                () -> assertEquals(bank + " " + requisites, actual.getDetails()),
                () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(uuid, actual.getMerchantOrderId())
        );
    }
}