package tgb.cryptoexchange.merchantdetails.details.evopay;

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
import tgb.cryptoexchange.merchantdetails.details.RequisiteRequest;
import tgb.cryptoexchange.merchantdetails.details.RequisiteResponse;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.EvoPayProperties;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvoPayOrderCreationServiceTest {

    @Mock
    private EvoPayProperties evoPayProperties;

    @InjectMocks
    private EvoPayOrderCreationService evoPayOrderCreationService;

    @Test
    void getMerchantShouldReturnEvoPayMerchant() {
        assertEquals(Merchant.EVO_PAY, evoPayOrderCreationService.getMerchant());
    }

    @Test
    void uriBuilderShouldAddUriPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/v1/api/order/payin", evoPayOrderCreationService.uriBuilder().apply(uriBuilder).getPath());
    }

    @CsvSource({
            "JQX1BI3Vs36UnMB,555",
            "y701U9erXYfOAdX,158",
            "w1vGjx4COVk531JZgj6dsh7uT,901"
    })
    @ParameterizedTest
    void headersShouldAddRequiredHeadersWithLessThan1000Amount(String key, Integer amount) {
        when(evoPayProperties.changeKey()).thenReturn(key);
        HttpHeaders headers = new HttpHeaders();
        RequisiteRequest requisiteRequest = new RequisiteRequest();
        requisiteRequest.setAmount(amount);
        evoPayOrderCreationService.headers(requisiteRequest, null).accept(headers);
        assertAll(
                () -> assertEquals(key, Objects.requireNonNull(headers.get("x-api-key")).getFirst())
        );
    }

    @CsvSource({
            "JQX1BI3Vs36UnMB,1001",
            "y701U9erXYfOAdX,15855",
            "w1vGjx4COVk531JZgj6dsh7uT,2700"
    })
    @ParameterizedTest
    void headersShouldAddRequiredHeadersWithMoreThan1000Amount(String key, Integer amount) {
        when(evoPayProperties.key()).thenReturn(key);
        HttpHeaders headers = new HttpHeaders();
        RequisiteRequest requisiteRequest = new RequisiteRequest();
        requisiteRequest.setAmount(amount);
        evoPayOrderCreationService.headers(requisiteRequest, null).accept(headers);
        assertAll(
                () -> assertEquals(key, Objects.requireNonNull(headers.get("x-api-key")).getFirst())
        );
    }

    @CsvSource({
            "2100,https://gateway.paysendmmm.online/merchant/evopay,BANK_CARD",
            "2100,https://someaddress.online/merchant/evopay,SBP"
    })
    @ParameterizedTest
    void bodyShouldReturnMappedBody(Integer amount, String callbackUrl, String method) {
        RequisiteRequest requisiteRequest = new RequisiteRequest();
        requisiteRequest.setAmount(amount);
        requisiteRequest.setCallbackUrl(callbackUrl);
        requisiteRequest.setMethod(method);
        Request request = evoPayOrderCreationService.body(requisiteRequest);
        assertAll(
                () -> assertDoesNotThrow(() -> UUID.fromString(request.getCustomId())),
                () -> assertEquals(Method.valueOf(method), request.getPaymentMethod()),
                () -> assertEquals(amount, request.getFiatSum())
        );
    }

    @CsvSource({
            "7cc259a2-67a2-4a67-8e7e-e0342c08da81,CREATED,1234123412341234,5001,Альфа-банк",
            "418adf2c-c382-42f4-8bbc-0d0ea008f701,IN_PROCESS,9876987654325432,1222,Сбербанк"
    })
    @ParameterizedTest
    void buildResponseShouldBuildRequisiteResponseWithCard(String id, Status status, String requisiteString, String bank) {
        Response response = new Response();
        response.setMethod(Method.BANK_CARD);
        response.setId(id);
        Response.Requisites requisites = new Response.Requisites();
        requisites.setRecipientBank(bank);
        requisites.setRecipientCardNumber(requisiteString);
        response.setRequisites(requisites);
        response.setOrderStatus(status);

        Optional<RequisiteResponse> maybeRequisiteResponse = evoPayOrderCreationService.buildResponse(response);
        assertTrue(maybeRequisiteResponse.isPresent());
        RequisiteResponse actual = maybeRequisiteResponse.get();
        assertAll(
                () -> assertEquals(Merchant.EVO_PAY, actual.getMerchant()),
                () -> assertEquals(id, actual.getMerchantOrderId()),
                () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(bank + " " + requisiteString, actual.getRequisite())
        );
    }

    @CsvSource({
            "7cc259a2-67a2-4a67-8e7e-e0342c08da81,CREATED,78984512,5001,Альфа-банк",
            "418adf2c-c382-42f4-8bbc-0d0ea008f701,IN_PROCESS,73215498,1222,Сбербанк"
    })
    @ParameterizedTest
    void buildResponseShouldBuildRequisiteResponseWithSbp(String id, Status status, String requisiteString, String bank) {
        Response response = new Response();
        response.setMethod(Method.SBP);
        response.setId(id);
        Response.Requisites requisites = new Response.Requisites();
        requisites.setRecipientBank(bank);
        requisites.setRecipientPhoneNumber(requisiteString);
        response.setRequisites(requisites);
        response.setOrderStatus(status);

        Optional<RequisiteResponse> maybeRequisiteResponse = evoPayOrderCreationService.buildResponse(response);
        assertTrue(maybeRequisiteResponse.isPresent());
        RequisiteResponse actual = maybeRequisiteResponse.get();
        assertAll(
                () -> assertEquals(Merchant.EVO_PAY, actual.getMerchant()),
                () -> assertEquals(id, actual.getMerchantOrderId()),
                () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(bank + " " + requisiteString, actual.getRequisite())
        );
    }

    @Test
    void buildResponseShouldReturnEmptyOptionalIfRequisitesIsNull() {
        Response response = new Response();
        assertTrue(evoPayOrderCreationService.buildResponse(response).isEmpty());
    }

    @Test
    void buildResponseShouldReturnEmptyOptionalIfRecipientBankIsNull() {
        Response response = new Response();
        Response.Requisites requisites = new Response.Requisites();
        response.setRequisites(requisites);
        assertTrue(evoPayOrderCreationService.buildResponse(response).isEmpty());
    }

    @Test
    void buildResponseShouldReturnEmptyOptionalIfCardAndPhoneIsNull() {
        Response response = new Response();
        Response.Requisites requisites = new Response.Requisites();
        requisites.setRecipientBank("bank");
        response.setRequisites(requisites);
        assertTrue(evoPayOrderCreationService.buildResponse(response).isEmpty());
    }
}