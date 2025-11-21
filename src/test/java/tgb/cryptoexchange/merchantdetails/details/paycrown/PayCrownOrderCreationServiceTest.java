package tgb.cryptoexchange.merchantdetails.details.paycrown;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.exception.BodyMappingException;
import tgb.cryptoexchange.merchantdetails.exception.SignatureCreationException;
import tgb.cryptoexchange.merchantdetails.properties.PayCrownProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayCrownOrderCreationServiceTest {

    @Mock
    private PayCrownProperties payCrownProperties;

    @Mock
    private SignatureService signatureService;

    @Mock
    private CallbackConfig callbackConfig;

    private ObjectMapper objectMapper;

    @InjectMocks
    private PayCrownOrderCreationService payCrownOrderCreationService;

    @BeforeEach
    void setUp() {
        this.objectMapper = Mockito.mock(ObjectMapper.class);
        payCrownOrderCreationService.setObjectMapper(objectMapper);
    }

    @Test
    void getMerchantShouldReturnPayCrown() {
        assertEquals(Merchant.PAY_CROWN, payCrownOrderCreationService.getMerchant());
    }

    @Test
    void uriBuilderShouldAddPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals(
                "/api/order/deposit",
                payCrownOrderCreationService.uriBuilder(null).apply(uriBuilder).getPath()
        );
    }

    @Test
    void headersShouldThrowBodyMappingExceptionIfJsonProcessingExceptionWasThrown() throws JsonProcessingException {
        when(objectMapper.readTree(anyString())).thenThrow(JsonProcessingException.class);
        Consumer<HttpHeaders> headersConsumer = payCrownOrderCreationService.headers(null, "");
        HttpHeaders headers = new HttpHeaders();
        assertThrows(BodyMappingException.class, () -> headersConsumer.accept(headers));
    }

    @Test
    void headersShouldThrowSignatureCreationExceptionIfNoSuchAlgorithmExceptionWasThrown() throws NoSuchAlgorithmException, JsonProcessingException {
        ObjectNode objectNode = new ObjectNode(JsonNodeFactory.instance);
        objectNode.put("created_at", System.currentTimeMillis());
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setMethod(Method.CARD.name());
        detailsRequest.setAmount(1000);
        Consumer<HttpHeaders> headersConsumer = payCrownOrderCreationService.headers(detailsRequest, "");
        HttpHeaders headers = new HttpHeaders();
        when(objectMapper.readTree(anyString())).thenReturn(objectNode);
        when(payCrownProperties.merchantId()).thenReturn("merchantId");
        when(payCrownProperties.secret()).thenReturn("secret");
        when(signatureService.getMD5Hash(anyString())).thenThrow(NoSuchAlgorithmException.class);
        assertThrows(SignatureCreationException.class, () -> headersConsumer.accept(headers));
    }

    @CsvSource(textBlock = """
            SBP,5330,BTC24MONEY,YrC6bKPZxxe6QXMIJjyXgJp3a5TlGYA8,zzRd9Zmy9mkNjfo,\
            16ce4f4e1ad5442dcfd4488f02c7df2f6a55f130361506342ea7be3d4136b129,'{"amount":1000,"method":"CARD"}'
            CARD,12355,BULBA_BOT,EZ13OTROWskIn16dxtTwZ4gDqvZjy66H,PzeiDYNh1RTRD5d,\
            92003059a722e7632fc06d79b2c682849aa17195b617580464d048e12242c844,'{"amount":5443,"method":"SBP"}'
            """)
    @ParameterizedTest
    void headersShouldAddRequiredHeaders(Method method, Integer amount, String merchantId, String secret, String key,
                                         String signature, String body) throws JsonProcessingException, NoSuchAlgorithmException {
        when(payCrownProperties.merchantId()).thenReturn(merchantId);
        when(payCrownProperties.secret()).thenReturn(secret);
        when(payCrownProperties.key()).thenReturn(key);

        ObjectNode objectNode = new ObjectNode(JsonNodeFactory.instance);
        Long unixTimestamp = System.currentTimeMillis();
        objectNode.put("created_at", unixTimestamp);
        when(objectMapper.readTree(anyString())).thenReturn(objectNode);

        when(signatureService.getMD5Hash(anyString())).thenReturn(signature);

        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setMethod(method.name());
        detailsRequest.setAmount(amount);

        HttpHeaders headers = new HttpHeaders();
        Consumer<HttpHeaders> headersConsumer = payCrownOrderCreationService.headers(detailsRequest, body);
        headersConsumer.accept(headers);
        verify(objectMapper).readTree(body);
        verify(signatureService).getMD5Hash(amount + unixTimestamp + "rub" + merchantId + method.getValue() + secret);
        assertAll(
                () -> assertEquals(key, Objects.requireNonNull(headers.get("X-Api-Key")).getFirst()),
                () -> assertEquals(signature, Objects.requireNonNull(headers.get("X-Paycrown-Sign")).getFirst())
        );
    }

    @CsvSource(textBlock = """
            1550,CARD,https://gateway.paysendmmm.online,BTC24MONEY,BfFpfLSGX8lqydL
            5000,SBP,https://someaddress.online,bulba_btc_bot,fGM1uP8msgRvpjJ
            """)
    @ParameterizedTest
    void bodyShouldBuildRequestObject(Integer amount, Method method, String gatewayUrl, String merchantId, String secret) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setMethod(method.name());
        detailsRequest.setAmount(amount);
        when(payCrownProperties.merchantId()).thenReturn(merchantId);
        when(callbackConfig.getGatewayUrl()).thenReturn(gatewayUrl);
        when(callbackConfig.getCallbackSecret()).thenReturn(secret);

        Request actual = payCrownOrderCreationService.body(detailsRequest);

        assertAll(
                () -> assertEquals(amount, actual.getAmount()),
                () -> assertEquals(merchantId, actual.getMerchantId()),
                () -> assertEquals(method, actual.getMethod()),
                () -> assertEquals(gatewayUrl + "/merchant-details/callback?merchant=PAY_CROWN&secret=" + secret,
                        actual.getCallbackUrl()),
                () -> assertNotNull(actual.getCreatedAt())
        );
    }

    @CsvSource(textBlock = """
            6ede157e-faef-4833-94ee-14797a88296a,ALFA,1234123412341234
            afe7b277-98af-498f-a9f1-13ef279ea2d9,Т-Банк,79822488437
            """)
    @ParameterizedTest
    void buildResponseShouldBuildResponseObject(String id, String bank, String details) {
        Response response = new Response();
        Response.Data data = new Response.Data();
        data.setId(id);
        Response.Data.Requisites requisites = new Response.Data.Requisites();
        requisites.setBank(bank);
        requisites.setRequisitesString(details);
        response.setData(data);
        data.setRequisites(requisites);

        Optional<DetailsResponse> maybeResponse = payCrownOrderCreationService.buildResponse(response);
        assertTrue(maybeResponse.isPresent());
        DetailsResponse actual = maybeResponse.get();
        assertAll(
                () -> assertEquals(Status.NEW.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(id, actual.getMerchantOrderId()),
                () -> assertEquals(bank + " " + details, actual.getDetails()),
                () -> assertEquals(Merchant.PAY_CROWN, actual.getMerchant())
        );
    }
}