package tgb.cryptoexchange.merchantdetails.details.whitelabel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.enums.FiatCurrency;
import tgb.cryptoexchange.merchantdetails.details.RequisiteRequest;
import tgb.cryptoexchange.merchantdetails.details.RequisiteResponse;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.exception.BodyMappingException;
import tgb.cryptoexchange.merchantdetails.exception.SignatureCreationException;
import tgb.cryptoexchange.merchantdetails.properties.AlfaTeamProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlfaTeamMerchantCreationServiceTest {

    @Mock
    private AlfaTeamProperties alfaTeamProperties;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SignatureService signatureService;

    @InjectMocks
    private AlfaTeamMerchantCreationService alfaTeamMerchantCreationService;

    @Test
    void getMerchantShouldReturnAlfaTeam() {
        assertEquals(Merchant.ALFA_TEAM, alfaTeamMerchantCreationService.getMerchant());
    }

    @Test
    void uriBuilderShouldSetPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/api/merchant/invoices", alfaTeamMerchantCreationService.uriBuilder().apply(uriBuilder).getPath());
    }

    @CsvSource({
            "https://alfa.com,HiubKWrJW8ytPGRR0E4XficeZ0ChxXHf,4u7XPRW4GzPlL187,zNEkiEItlmOqjmKfKpc7S5aePt3W0gKA,TO_CARD,5012,https://paysendmmm.online/merchant/alfa,sign1",
            "https://alfa.merch.info,18td2niwHwNa2SOy3jhaQdWQrZeFgvy6,oNnM2Ud8RGA8FjCc,Y10kUUIc5b7Q2YyJyzE9SHx6iYtMYO9o,SBP,2244,https://gateway.paysendmmm.online/merch/alfa,someSign2"
    })
    @ParameterizedTest
    void headersShouldSetRequiredHeaders(String url, String secret, String key, String token, String method,
                                         Integer amount, String callbackUrl, String sign)
            throws NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {
        when(alfaTeamProperties.url()).thenReturn(url);
        when(alfaTeamProperties.key()).thenReturn(key);
        when(alfaTeamProperties.token()).thenReturn(token);
        when(alfaTeamProperties.secret()).thenReturn(secret);
        String expectedBody = "{\"field\": \"expectedBody\"}";
        ArgumentCaptor<Request> requestArgumentCaptor = ArgumentCaptor.forClass(Request.class);
        when(objectMapper.writeValueAsString(requestArgumentCaptor.capture())).thenReturn(expectedBody);
        ArgumentCaptor<String> dataArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> secretArgumentCaptor = ArgumentCaptor.forClass(String.class);
        when(signatureService.hmacSHA1(dataArgumentCaptor.capture(), secretArgumentCaptor.capture())).thenReturn(sign);

        HttpHeaders headers = new HttpHeaders();
        RequisiteRequest request = Mockito.mock(RequisiteRequest.class);
        when(request.getMethod()).thenReturn(method);
        when(request.getCallbackUrl()).thenReturn(callbackUrl);
        when(request.getAmount()).thenReturn(amount);
        alfaTeamMerchantCreationService.headers(request).accept(headers);
        Request actual = requestArgumentCaptor.getValue();
        assertAll(
                () -> assertEquals(sign, Objects.requireNonNull(headers.get("X-Signature")).getFirst()),
                () -> assertEquals("POST" + url + "/api/merchant/invoices" + expectedBody, dataArgumentCaptor.getValue()),
                () -> assertEquals(secret, secretArgumentCaptor.getValue()),
                () -> assertEquals(amount.toString(), actual.getAmount()),
                () -> assertEquals(FiatCurrency.RUB.name(), actual.getCurrency()),
                () -> assertEquals(callbackUrl, actual.getNotificationUrl()),
                () -> assertEquals(token, actual.getNotificationToken()),
                () -> assertDoesNotThrow(() -> UUID.fromString(actual.getInternalId())),
                () -> assertEquals(PaymentOption.valueOf(method), actual.getPaymentOption()),
                () -> assertTrue(actual.getStartDeal())
        );
    }

    @Test
    void headersShouldThrowBodyMappingException() throws JsonProcessingException {
        RequisiteRequest request = Mockito.mock(RequisiteRequest.class);
        when(request.getMethod()).thenReturn("TO_CARD");
        when(request.getCallbackUrl()).thenReturn("callbackUrl");
        when(request.getAmount()).thenReturn(1);
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonMappingException(""));
        HttpHeaders headers = new HttpHeaders();
        Consumer<HttpHeaders> headersConsumer = alfaTeamMerchantCreationService.headers(request);
        assertThrows(BodyMappingException.class, () -> headersConsumer.accept(headers));
    }

    @Test
    void headersShouldThrowSignatureCreationException() throws JsonProcessingException, NoSuchAlgorithmException, InvalidKeyException {
        RequisiteRequest request = Mockito.mock(RequisiteRequest.class);
        when(request.getMethod()).thenReturn("TO_CARD");
        when(request.getCallbackUrl()).thenReturn("callbackUrl");
        when(request.getAmount()).thenReturn(1);
        when(objectMapper.writeValueAsString(any())).thenReturn("");
        when(alfaTeamProperties.url()).thenReturn("");
        when(alfaTeamProperties.secret()).thenReturn("");
        when(signatureService.hmacSHA1(anyString(), anyString())).thenThrow(InvalidKeyException.class);
        HttpHeaders headers = new HttpHeaders();
        Consumer<HttpHeaders> headersConsumer = alfaTeamMerchantCreationService.headers(request);
        assertThrows(SignatureCreationException.class, () -> headersConsumer.accept(headers));
    }

    @Test
    void buildResponseShouldReturnEmptyOptionalIfDealsIsNull() {
        Response response = new Response();
        assertTrue(alfaTeamMerchantCreationService.buildResponse(response).isEmpty());
    }

    @Test
    void buildResponseShouldReturnEmptyOptionalIfDealsIsEmpty() {
        Response response = new Response();
        response.setDeals(new ArrayList<>());
        assertTrue(alfaTeamMerchantCreationService.buildResponse(response).isEmpty());
    }

    @CsvSource({
            "2ac093d0-49aa-4bc7-a7dc-61fa62820544,SBERBANK,1234123412341234",
            "9e6eee50-dcbb-47be-85a5-494db6d49157,UNISTREAM,79865431232",
            "070329a9-4f1b-4c83-a8f7-68a385bc23f3,RNCB,8888666644442222"
    })
    @ParameterizedTest
    void buildResponseShouldReturnMappedRequisiteDTO(String id, PaymentMethod paymentMethod, String requisite) {
        Response response = new Response();
        DealDTO dealDTO = new DealDTO();
        dealDTO.setPaymentMethod(paymentMethod);
        RequisitesDTO requisitesDTO = new RequisitesDTO();
        requisitesDTO.setRequisites(requisite);
        dealDTO.setRequisites(requisitesDTO);
        response.setDeals(List.of(dealDTO));
        response.setId(id);
        Optional<RequisiteResponse> actual = alfaTeamMerchantCreationService.buildResponse(response);
        assertTrue(actual.isPresent());
        RequisiteResponse actualResponse = actual.get();
        assertAll(
                () -> assertEquals(Merchant.ALFA_TEAM, actualResponse.getMerchant()),
                () -> assertEquals(id, actualResponse.getMerchantOrderId()),
                () -> assertEquals(InvoiceStatus.NEW.name(), actualResponse.getMerchantOrderStatus()),
                () -> assertEquals(paymentMethod.getDisplayName() + " " + requisite, actualResponse.getRequisite())
        );
    }
}