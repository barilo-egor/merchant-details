package tgb.cryptoexchange.merchantdetails.details.wellbit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.exception.ServiceUnavailableException;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.exception.SignatureCreationException;
import tgb.cryptoexchange.merchantdetails.properties.WellBitProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WellBitOrderCreationServiceTest {

    @Mock
    private SignatureService signatureService;

    @Mock
    private WellBitProperties wellBitProperties;

    @InjectMocks
    private WellBitOrderCreationService wellBitOrderCreationService;

    @Test
    void getMerchantShouldReturnWellBit() {
        assertEquals(Merchant.WELL_BIT, wellBitOrderCreationService.getMerchant());
    }

    @Test
    void uriBuilderShouldAddPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals(
                "/api/payment/make",
                wellBitOrderCreationService.uriBuilder(null).apply(uriBuilder).getPath()
        );
    }

    @CsvSource(textBlock = """
            aEBG781htCAMIyBvf2cuOc7cgGXelyp4,I7Sr8pMloYE8tYdFXPUGE97l3pjCnjWW,BTC24MONEY,3b9rAQ5jM0,\
            j4rsekj965ckme289kdmbp9lvakqpqva
            JC8je6MgZj7dkuC7hT3dqsSto28MaGtV,rfcS3LU8fUn7Kl4tPIudzL1D7shCzcew,BULBA_BOT,5zTquP95RD,\
            2axdfoqf0963dkmhy7vv25ky4r0xt2l9
            """)
    @ParameterizedTest
    void headersShouldAddRequiredHeaders(String token, String secret, String login, String id, String hash) throws NoSuchAlgorithmException {
        when(wellBitProperties.token()).thenReturn(token);
        when(wellBitProperties.secret()).thenReturn(secret);
        when(wellBitProperties.login()).thenReturn(login);
        when(wellBitProperties.id()).thenReturn(id);
        when(signatureService.getMD5Hash(anyString())).thenReturn(hash);
        HttpHeaders headers = new HttpHeaders();

        wellBitOrderCreationService.headers(null, null).accept(headers);

        verify(signatureService).getMD5Hash(secret + login + id);
        assertAll(
                () -> assertEquals(token, headers.getFirst("token")),
                () -> assertEquals(hash, headers.getFirst("secret"))
        );
    }

    @CsvSource(textBlock = """
            aEBG781htCAMIyBvf2cuOc7cgGXelyp4,I7Sr8pMloYE8tYdFXPUGE97l3pjCnjWW,BTC24MONEY,3b9rAQ5jM0,\
            j4rsekj965ckme289kdmbp9lvakqpqva
            JC8je6MgZj7dkuC7hT3dqsSto28MaGtV,rfcS3LU8fUn7Kl4tPIudzL1D7shCzcew,BULBA_BOT,5zTquP95RD,\
            2axdfoqf0963dkmhy7vv25ky4r0xt2l9
            """)
    @ParameterizedTest
    void headersShouldAddCachedHashSecret(String token, String secret, String login, String id, String hash) throws NoSuchAlgorithmException {
        when(wellBitProperties.token()).thenReturn(token);
        when(wellBitProperties.secret()).thenReturn(secret);
        when(wellBitProperties.login()).thenReturn(login);
        when(wellBitProperties.id()).thenReturn(id);
        when(signatureService.getMD5Hash(anyString())).thenReturn(hash);
        HttpHeaders headers = new HttpHeaders();

        wellBitOrderCreationService.headers(null, null).accept(headers);
        wellBitOrderCreationService.headers(null, null).accept(headers);
        verify(signatureService).getMD5Hash(secret + login + id);
    }


    @Test
    void headersShouldThrowSignatureCreationExceptionIfNoSuchAlgorithmExceptionThrown() throws NoSuchAlgorithmException {
        when(wellBitProperties.secret()).thenReturn("secret");
        when(wellBitProperties.login()).thenReturn("login");
        when(wellBitProperties.id()).thenReturn("id");
        when(signatureService.getMD5Hash(anyString())).thenThrow(NoSuchAlgorithmException.class);
        HttpHeaders headers = new HttpHeaders();
        Consumer<HttpHeaders> headersConsumer = wellBitOrderCreationService.headers(null, null);
        assertThrows(SignatureCreationException.class, () -> headersConsumer.accept(headers));
    }

    @CsvSource(textBlock = """
            5001,SBP
            2503,CARDS
            """)
    @ParameterizedTest
    void bodyShouldBuildRequestObject(Integer amount, Method method) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setMethods(List.of(DetailsRequest.MerchantMethod.builder().merchant(Merchant.WELL_BIT).method(method.name()).build()));
        detailsRequest.setAmount(amount);

        Request actual = wellBitOrderCreationService.body(detailsRequest);

        assertAll(
                () -> assertEquals("yes", actual.getCredentialRequire()),
                () -> assertEquals(amount, actual.getAmount()),
                () -> assertEquals(method.getValue(), actual.getCredentialType()),
                () -> assertDoesNotThrow(() -> UUID.fromString(actual.getCustomNumber())),
                () -> assertEquals("-", actual.getClientIp()),
                () -> assertEquals("-", actual.getCardFromFio()),
                () -> assertEquals("-", actual.getClientEmail()),
                () -> assertEquals("-", actual.getCardFromNumber())
        );
    }

    @CsvSource(textBlock = """
            ALFA,79878765432,NEW,553425
            SBER,9999888877776666,COMPLETE,1244
            """)
    @ParameterizedTest
    void buildResponseShouldBuildResponseObject(String bank, String credential, Status status, Long id) {
        Response response = new Response();
        Response.Payment payment = new Response.Payment();
        response.setPayment(payment);
        payment.setId(id);
        payment.setStatus(status);
        payment.setCredentialAdditionalBank(bank);
        payment.setCredential(credential);

        Optional<DetailsResponse> maybeResponse = wellBitOrderCreationService.buildResponse(response);
        assertTrue(maybeResponse.isPresent());
        DetailsResponse actual = maybeResponse.get();
        assertAll(
            () -> assertEquals(Merchant.WELL_BIT, actual.getMerchant()),
            () -> assertEquals(bank + " " + credential, actual.getDetails()),
            () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
            () -> assertEquals(id.toString(), actual.getMerchantOrderId())
        );
    }

    @Test
    void hasResponseNoDetailsErrorPredicateShouldReturnTrueIfE0010Code() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        wellBitOrderCreationService.setObjectMapper(objectMapper);
        JsonNode jsonNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(jsonNode);
        when(jsonNode.isArray()).thenReturn(true);
        JsonNode firstJsonNode = Mockito.mock(JsonNode.class);
        when(jsonNode.get(0)).thenReturn(firstJsonNode);
        when(firstJsonNode.has("code")).thenReturn(true);
        JsonNode codeJsonNode = Mockito.mock(JsonNode.class);
        when(firstJsonNode.get("code")).thenReturn(codeJsonNode);
        when(codeJsonNode.asText()).thenReturn("E0010");
        assertTrue(wellBitOrderCreationService.hasResponseNoDetailsErrorPredicate().test(""));
    }

    @Test
    void hasResponseNoDetailsErrorPredicateShouldReturnFalseIfCodeNotE0010() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        wellBitOrderCreationService.setObjectMapper(objectMapper);
        JsonNode jsonNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(jsonNode);
        when(jsonNode.isArray()).thenReturn(true);
        JsonNode firstJsonNode = Mockito.mock(JsonNode.class);
        when(jsonNode.get(0)).thenReturn(firstJsonNode);
        when(firstJsonNode.has("code")).thenReturn(true);
        JsonNode codeJsonNode = Mockito.mock(JsonNode.class);
        when(firstJsonNode.get("code")).thenReturn(codeJsonNode);
        when(codeJsonNode.asText()).thenReturn("E0011");
        assertFalse(wellBitOrderCreationService.hasResponseNoDetailsErrorPredicate().test(""));
    }

    @Test
    void hasResponseNoDetailsPredicateShouldReturnFalseIfNoCodeField() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        wellBitOrderCreationService.setObjectMapper(objectMapper);
        JsonNode jsonNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(jsonNode);
        when(jsonNode.isArray()).thenReturn(true);
        JsonNode firstJsonNode = Mockito.mock(JsonNode.class);
        when(jsonNode.get(0)).thenReturn(firstJsonNode);
        when(firstJsonNode.has("code")).thenReturn(false);
        assertFalse(wellBitOrderCreationService.hasResponseNoDetailsErrorPredicate().test(""));
    }

    @Test
    void hasResponseNoDetailsPredicateShouldReturnFalseIfArrayEmpty() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        wellBitOrderCreationService.setObjectMapper(objectMapper);
        JsonNode jsonNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(jsonNode);
        when(jsonNode.isArray()).thenReturn(true);
        when(jsonNode.isEmpty()).thenReturn(true);
        assertFalse(wellBitOrderCreationService.hasResponseNoDetailsErrorPredicate().test(""));
    }

    @Test
    void hasResponseNoDetailsPredicateShouldReturnFalseIfResponseIsNotArray() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        wellBitOrderCreationService.setObjectMapper(objectMapper);
        JsonNode jsonNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(jsonNode);
        when(jsonNode.isArray()).thenReturn(false);
        assertFalse(wellBitOrderCreationService.hasResponseNoDetailsErrorPredicate().test(""));
    }

    @Test
    void hasResponseNoDetailsErrorPredicateShouldThrowServiceUnavailableExceptionIfJsonProcessingExceptionWasThrown() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        wellBitOrderCreationService.setObjectMapper(objectMapper);
        when(objectMapper.readTree(anyString())).thenThrow(JsonProcessingException.class);
        Predicate<String> hasResponseNoDetailsErrorPredicate = wellBitOrderCreationService.hasResponseNoDetailsErrorPredicate();
        assertThrows(ServiceUnavailableException.class, () -> hasResponseNoDetailsErrorPredicate.test(""));
    }
}