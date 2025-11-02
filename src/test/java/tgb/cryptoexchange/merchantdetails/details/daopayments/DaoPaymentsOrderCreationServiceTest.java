package tgb.cryptoexchange.merchantdetails.details.daopayments;

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
import tgb.cryptoexchange.merchantdetails.properties.DaoPaymentsProperties;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DaoPaymentsOrderCreationServiceTest {

    @Mock
    private DaoPaymentsProperties daoPaymentsProperties;

    @InjectMocks
    private DaoPaymentsOrderCreationService daoPaymentsOrderCreationService;


    @Test
    void getMerchantShouldReturnCrocoPayMerchant() {
        assertEquals(Merchant.DAO_PAYMENTS, daoPaymentsOrderCreationService.getMerchant());
    }

    @Test
    void uriBuilderShouldAddUriPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/api/v1/deposit", daoPaymentsOrderCreationService.uriBuilder(null).apply(uriBuilder).getPath());
    }

    @CsvSource({
            "JQX1BI3Vs36UnMB",
            "y701U9erXYfOAdX",
            "w1vGjx4COVk531JZgj6dsh7uT"
    })
    @ParameterizedTest
    void headersShouldAddRequiredHeaders(String key) {
        when(daoPaymentsProperties.key()).thenReturn(key);
        HttpHeaders headers = new HttpHeaders();
        daoPaymentsOrderCreationService.headers(null, null).accept(headers);
        assertAll(
                () -> assertEquals("application/json", Objects.requireNonNull(headers.get("Content-Type")).getFirst()),
                () -> assertEquals(key, Objects.requireNonNull(headers.get("X-API-KEY")).getFirst())
        );
    }

    @CsvSource({
            "2100,https://gateway.paysendmmm.online/merchant/appexbit,CARD",
            "2100,https://someaddress.online/merchant/appexbit,SBP"
    })
    @ParameterizedTest
    void bodyShouldReturnMappedBody(Integer amount, String callbackUrl, String method) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setCallbackUrl(callbackUrl);
        detailsRequest.setMethod(method);
        Request request = daoPaymentsOrderCreationService.body(detailsRequest);
        assertAll(
                () -> assertDoesNotThrow(() -> UUID.fromString(request.getMerchantOrderId())),
                () -> assertEquals(Method.valueOf(method), request.getRequisiteType()),
                () -> assertEquals(amount.toString(), request.getAmount()),
                () -> assertEquals(callbackUrl, request.getSuccessUrl()),
                () -> assertEquals(callbackUrl, request.getFailUrl())
        );
    }

    @CsvSource({
            "7cc259a2-67a2-4a67-8e7e-e0342c08da81,PENDING,1234123412341234,5001,Альфа-банк",
            "418adf2c-c382-42f4-8bbc-0d0ea008f701,CHECKING,9876987654325432,1222,Сбербанк"
    })
    @ParameterizedTest
    void buildResponseShouldBuildRequisiteResponse(String id, Status status, String requisiteString, Integer amount, String bank) {
        Response response = new Response();
        response.setTransactionId(id);
        response.setStatus(status);
        response.setAmount(amount.toString());
        Response.TransferDetails transferDetails = new Response.TransferDetails();
        transferDetails.setBankName(bank);
        transferDetails.setCardNumber(requisiteString);
        response.setTransferDetails(transferDetails);

        Optional<DetailsResponse> maybeRequisiteResponse = daoPaymentsOrderCreationService.buildResponse(response);
        assertTrue(maybeRequisiteResponse.isPresent());
        DetailsResponse actual = maybeRequisiteResponse.get();
        assertAll(
                () -> assertEquals(Merchant.DAO_PAYMENTS, actual.getMerchant()),
                () -> assertEquals(id, actual.getMerchantOrderId()),
                () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(bank + " " + requisiteString, actual.getDetails())
        );
    }

    @Test
    void buildResponseShouldReturnEmptyOptionalIfTransferDetailsIsNull() {
        Response response = new Response();
        assertTrue(daoPaymentsOrderCreationService.buildResponse(response).isEmpty());
    }

    @Test
    void buildResponseShouldReturnEmptyOptionalIfBankNameIsNull() {
        Response response = new Response();
        Response.TransferDetails transferDetails = new Response.TransferDetails();
        response.setTransferDetails(transferDetails);
        assertTrue(daoPaymentsOrderCreationService.buildResponse(response).isEmpty());
    }

    @Test
    void buildResponseShouldReturnEmptyOptionalIfCardNumberIsNull() {
        Response response = new Response();
        Response.TransferDetails transferDetails = new Response.TransferDetails();
        transferDetails.setBankName("bank");
        response.setTransferDetails(transferDetails);
        assertTrue(daoPaymentsOrderCreationService.buildResponse(response).isEmpty());
    }
}