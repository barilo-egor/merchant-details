package tgb.cryptoexchange.merchantdetails.details.crocopay;

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
import tgb.cryptoexchange.merchantdetails.properties.CrocoPayProperties;

import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrocoPayOrderCreationServiceTest {

    @Mock
    private CrocoPayProperties crocoPayProperties;

    @InjectMocks
    private CrocoPayOrderCreationService crocoPayOrderCreationService;


    @Test
    void getMerchantShouldReturnCrocoPayMerchant() {
        assertEquals(Merchant.CROCO_PAY, crocoPayOrderCreationService.getMerchant());
    }

    @Test
    void uriBuilderShouldAddUriPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/api/v2/h2h/invoices", crocoPayOrderCreationService.uriBuilder().apply(uriBuilder).getPath());
    }

    @CsvSource({
            "BTC24MONEY,JQX1BI3Vs36UnMB",
            "4aSoUSDBz2,y701U9erXYfOAdX",
            "id,w1vGjx4COVk531JZgj6dsh7uT"
    })
    @ParameterizedTest
    void headersShouldAddRequiredHeaders(String id, String secret) {
        when(crocoPayProperties.clientId()).thenReturn(id);
        when(crocoPayProperties.clientSecret()).thenReturn(secret);
        HttpHeaders headers = new HttpHeaders();
        crocoPayOrderCreationService.headers(null, null).accept(headers);
        assertAll(
                () -> assertEquals(id, Objects.requireNonNull(headers.get("Client-Id")).getFirst()),
                () -> assertEquals(secret, Objects.requireNonNull(headers.get("Client-Secret")).getFirst())
        );
    }

    @CsvSource({
            "2100,TO_CARD",
            "2100,SBP"
    })
    @ParameterizedTest
    void bodyShouldReturnMappedBody(Integer amount, String method) {
        RequisiteRequest requisiteRequest = new RequisiteRequest();
        requisiteRequest.setAmount(amount);
        requisiteRequest.setMethod(method);
        Request request = crocoPayOrderCreationService.body(requisiteRequest);
        assertAll(
                () -> assertEquals(amount, request.getAmount()),
                () -> assertEquals(Method.valueOf(method), request.getMethod())
        );
    }

    @CsvSource({
            "7cc259a2-67a2-4a67-8e7e-e0342c08da81,PENDING,1234123412341234",
            "418adf2c-c382-42f4-8bbc-0d0ea008f701,SUCCESS,9876987654325432"
    })
    @ParameterizedTest
    void buildResponseShouldBuildRequisiteResponseWithAnyRubBank(String id, Status status, String requisiteString) {
        Response response = new Response();
        Response.ResponseData responseData = new Response.ResponseData();
        Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setStatus(status);
        responseData.setTransaction(transaction);
        Response.ResponseData.PaymentRequisites paymentRequisites = new Response.ResponseData.PaymentRequisites();
        paymentRequisites.setRequisites(requisiteString);
        paymentRequisites.setPaymentMethod("any_rub_bank");
        responseData.setPaymentRequisites(paymentRequisites);
        response.setResponseData(responseData);

        Optional<RequisiteResponse> maybeRequisiteResponse = crocoPayOrderCreationService.buildResponse(response);
        assertTrue(maybeRequisiteResponse.isPresent());
        RequisiteResponse actual = maybeRequisiteResponse.get();
        assertAll(
                () -> assertEquals(Merchant.CROCO_PAY, actual.getMerchant()),
                () -> assertEquals(id, actual.getMerchantOrderId()),
                () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(requisiteString, actual.getRequisite())
        );
    }

    @CsvSource({
            "7cc259a2-67a2-4a67-8e7e-e0342c08da81,PENDING,1234123412341234,ALFA",
            "418adf2c-c382-42f4-8bbc-0d0ea008f701,SUCCESS,9876987654325432,SBER"
    })
    @ParameterizedTest
    void buildResponseShouldBuildRequisiteResponseWithNotAnyRubBank(String requisiteString, String bank) {
        Response response = new Response();
        Response.ResponseData responseData = new Response.ResponseData();
        Transaction transaction = new Transaction();
        transaction.setId("id");
        transaction.setStatus(Status.DISPUTE);
        responseData.setTransaction(transaction);
        Response.ResponseData.PaymentRequisites paymentRequisites = new Response.ResponseData.PaymentRequisites();
        paymentRequisites.setRequisites(requisiteString);
        paymentRequisites.setPaymentMethod(bank);
        responseData.setPaymentRequisites(paymentRequisites);
        response.setResponseData(responseData);

        Optional<RequisiteResponse> maybeRequisiteResponse = crocoPayOrderCreationService.buildResponse(response);
        assertTrue(maybeRequisiteResponse.isPresent());
        RequisiteResponse actual = maybeRequisiteResponse.get();
        assertAll(
                () -> assertEquals(bank + " " + requisiteString, actual.getRequisite())
        );
    }

    @Test
    void buildResponseShouldReturnEmptyOptionalIfResponseDataIsNull() {
        Response response = new Response();
        assertTrue(crocoPayOrderCreationService.buildResponse(response).isEmpty());
    }

    @Test
    void buildResponseShouldReturnEmptyOptionalIfPaymentRequisitesIsNull() {
        Response response = new Response();
        response.setResponseData(new Response.ResponseData());
        assertTrue(crocoPayOrderCreationService.buildResponse(response).isEmpty());
    }

    @Test
    void buildResponseShouldReturnEmptyOptionalIfPaymentMethodIsNull() {
        Response response = new Response();
        Response.ResponseData responseData = new Response.ResponseData();
        responseData.setPaymentRequisites(new Response.ResponseData.PaymentRequisites());
        response.setResponseData(responseData);
        assertTrue(crocoPayOrderCreationService.buildResponse(response).isEmpty());
    }

    @Test
    void buildResponseShouldReturnEmptyOptionalIfRequisitesOfPaymentRequisitesIsNull() {
        Response response = new Response();
        Response.ResponseData responseData = new Response.ResponseData();
        Response.ResponseData.PaymentRequisites paymentRequisites = new Response.ResponseData.PaymentRequisites();
        paymentRequisites.setPaymentMethod("method");
        responseData.setPaymentRequisites(paymentRequisites);
        response.setResponseData(responseData);
        assertTrue(crocoPayOrderCreationService.buildResponse(response).isEmpty());
    }
}