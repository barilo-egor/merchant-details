package tgb.cryptoexchange.merchantdetails.details.bitzone;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.merchantdetails.details.RequisiteRequest;
import tgb.cryptoexchange.merchantdetails.details.RequisiteResponse;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.BitZoneProperties;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BitZoneOrderCreationServiceTest {

    @Mock
    private BitZoneProperties bitZoneProperties;

    @InjectMocks
    private BitZoneOrderCreationService bitZoneOrderCreationService;

    @Test
    void getMerchantShouldReturnAppexbitMerchant() {
        assertEquals(Merchant.BIT_ZONE, bitZoneOrderCreationService.getMerchant());
    }

    @Test
    void uriBuilderShouldAddUriPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/payment/trading/pay-in", bitZoneOrderCreationService.uriBuilder().apply(uriBuilder).getPath());
    }

    @ValueSource(strings = {
            "JQX1BI3Vs36UnMB", "y701U9erXYfOAdX", "k531JZgj6dsh7uT"
    })
    @ParameterizedTest
    void headersShouldAddRequiredHeaders(String key) {
        when(bitZoneProperties.key()).thenReturn(key);
        HttpHeaders headers = new HttpHeaders();
        bitZoneOrderCreationService.headers(null, "").accept(headers);
        assertAll(
                () -> assertEquals(key, Objects.requireNonNull(headers.get("x-api-key")).getFirst()),
                () -> assertEquals("application/json", Objects.requireNonNull(headers.get("Content-Type")).getFirst())
        );
    }

    @CsvSource({
            "2100,CARD",
            "2100,SBP"
    })
    @ParameterizedTest
    void bodyShouldReturnMappedBody(Integer amount, String method) {
        RequisiteRequest requisiteRequest = new RequisiteRequest();
        requisiteRequest.setAmount(amount);
        requisiteRequest.setMethod(method);
        Request request = bitZoneOrderCreationService.body(requisiteRequest);
        assertAll(
                () -> assertEquals(amount, request.getFiatAmount()),
                () -> assertEquals(Method.valueOf(method), request.getMethod()),
                () -> assertDoesNotThrow(() -> UUID.fromString(request.getExtra().getExternalTransactionId()))
        );
    }

    @CsvSource({
            "7cc259a2-67a2-4a67-8e7e-e0342c08da81,PENDING,1234123412341234,ALFA",
            "418adf2c-c382-42f4-8bbc-0d0ea008f701,ACTIVE,9876987654325432,SBER"
    })
    @ParameterizedTest
    void buildResponseShouldBuildRequisiteResponseWithCard(String id, Status status, String requisiteString, String bank) {
        Response response = new Response();
        response.setId(id);
        response.setStatus(status);
        response.setMethod(Method.CARD);
        Response.Requisite requisite = new Response.Requisite();
        requisite.setBank(bank);
        requisite.setRequisites(requisiteString);
        response.setRequisite(requisite);

        Optional<RequisiteResponse> maybeRequisiteResponse = bitZoneOrderCreationService.buildResponse(response);
        assertTrue(maybeRequisiteResponse.isPresent());
        RequisiteResponse actual = maybeRequisiteResponse.get();
        assertAll(
                () -> assertEquals(Merchant.BIT_ZONE, actual.getMerchant()),
                () -> assertEquals(id, actual.getMerchantOrderId()),
                () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(bank + " " + requisiteString, actual.getRequisite())
        );
    }

    @CsvSource({
            "79284565465,ALFA",
            "147896541,SBER"
    })
    @ParameterizedTest
    void buildResponseShouldBuildRequisiteResponseWithSbp(String requisiteString, String bank) {
        Response response = new Response();
        response.setId("id");
        response.setStatus(Status.ACTIVE);
        response.setMethod(Method.SBP);
        Response.Requisite requisite = new Response.Requisite();
        requisite.setBank(bank);
        requisite.setSbpNumber(requisiteString);
        response.setRequisite(requisite);

        Optional<RequisiteResponse> maybeRequisiteResponse = bitZoneOrderCreationService.buildResponse(response);
        assertTrue(maybeRequisiteResponse.isPresent());
        RequisiteResponse actual = maybeRequisiteResponse.get();
        assertAll(
                () -> assertEquals(bank + " " + requisiteString, actual.getRequisite())
        );
    }

    @Test
    void buildResponseShouldReturnEmptyOptionalIfRequisiteIsNull() {
        Response response = new Response();
        assertTrue(bitZoneOrderCreationService.buildResponse(response).isEmpty());
    }

    @Test
    void buildResponseShouldReturnEmptyOptionalIfBankIsNull() {
        Response response = new Response();
        response.setRequisite(new Response.Requisite());
        assertTrue(bitZoneOrderCreationService.buildResponse(response).isEmpty());
    }

    @Test
    void buildResponseShouldReturnEmptyOptionalIfRequisitesAndSbpNumberIsNull() {
        Response response = new Response();
        response.setRequisite(new Response.Requisite());
        response.getRequisite().setBank("bank");
        assertTrue(bitZoneOrderCreationService.buildResponse(response).isEmpty());
    }
}