package tgb.cryptoexchange.merchantdetails.details.nicepay;

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
import tgb.cryptoexchange.merchantdetails.constants.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.NicePayProperties;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NicePayOrderCreationServiceTest {

    @Mock
    private NicePayProperties nicePayProperties;

    @InjectMocks
    private NicePayOrderCreationService nicePayOrderCreationService;

    @Test
    void getMerchantShouldReturnNicePay() {
        assertEquals(Merchant.NICE_PAY, nicePayOrderCreationService.getMerchant());
    }

    @Test
    void uriBuilderShouldAddPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/h2hOneRequestPayment", nicePayOrderCreationService.uriBuilder(null).apply(uriBuilder).getPath());
    }

    @Test
    void headersShouldAddRequiredHeaders() {
        HttpHeaders headers = new HttpHeaders();
        nicePayOrderCreationService.headers(null, null).accept(headers);
        assertEquals("application/json", Objects.requireNonNull(headers.get("Content-Type")).getFirst());
    }

    @ParameterizedTest
    @CsvSource({
            "5440,9mkInR6wI2wBWS7,i3UqOd70HdDWmY9iY8HyPhEaibLCpsi8",
            "5440,eerflhGHcuQtX3T,MZBi6sS53B9G1hwJ9vnA9wKLh8vRSkLV"
    })
    void bodyShouldBuildRequestObjectForSbpRu(Integer amount, String merchantId, String secret) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setMethods(List.of(DetailsRequest.MerchantMethod.builder().merchant(Merchant.NICE_PAY).method(Method.SBP_RU.name()).build()));
        when(nicePayProperties.merchantId()).thenReturn(merchantId);
        when(nicePayProperties.secret()).thenReturn(secret);

        Request actual = nicePayOrderCreationService.body(detailsRequest);
        assertAll(
                () -> assertEquals(merchantId, actual.getMerchantId()),
                () -> assertEquals(secret, actual.getSecret()),
                () -> assertDoesNotThrow(() -> UUID.fromString(actual.getOrderId())),
                () -> assertEquals(amount * 100, actual.getAmount()),
                () -> assertEquals(Method.SBP_RU, actual.getMethod()),
                () -> assertEquals("onlyRU", actual.getMethodSBP())
        );
    }

    @Test
    void bodyShouldBuildRequestObjectForSbpTransgran() {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(1);
        detailsRequest.setMethods(List.of(DetailsRequest.MerchantMethod.builder().merchant(Merchant.NICE_PAY).method(Method.SBP_TRANSGRAN.name()).build()));
        when(nicePayProperties.merchantId()).thenReturn("merchantId");
        when(nicePayProperties.secret()).thenReturn("secret");

        Request actual = nicePayOrderCreationService.body(detailsRequest);
        assertAll(
                () -> assertEquals("onlyINT", actual.getMethodSBP())
        );
    }

    @Test
    void bodyShouldBuildRequestObjectWithNullMethodSBP() {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(1);
        detailsRequest.setMethods(List.of(DetailsRequest.MerchantMethod.builder().merchant(Merchant.NICE_PAY).method(Method.SBP.name()).build()));
        when(nicePayProperties.merchantId()).thenReturn("merchantId");
        when(nicePayProperties.secret()).thenReturn("secret");

        Request actual = nicePayOrderCreationService.body(detailsRequest);
        assertNull(actual.getMethodSBP());
    }

    @ParameterizedTest
    @CsvSource({
            "CREATED,fflgpFsdREkhVdc6dGL6cmsS9GF0CDi4,Сбербанк,78752341232",
            "AWAITING_PAYMENT,x0lT6Mwrg5FBZUv6zs0QaiBuU0GKh4ew,T-BANK,72340983498"
    })
    void buildResponseShouldBuildResponseObjectWithSubMethod(Status status, String paymentId, String bank, String detailsString) {
        Response response = new Response();
        response.setStatus(Response.ResponseStatus.DETAILS_FOUND);
        Response.Data data = new Response.Data();
        response.setData(data);
        data.setStatus(status);
        data.setPaymentId(paymentId);
        Response.Data.SubMethod subMethod = new Response.Data.SubMethod();
        Response.Data.SubMethod.Names names = new Response.Data.SubMethod.Names();
        names.setRu(bank);
        subMethod.setNames(names);
        data.setSubMethod(subMethod);
        Response.Data.Details details = new Response.Data.Details();
        details.setWallet(detailsString);
        data.setDetails(details);

        Optional<DetailsResponse> maybeResponse = nicePayOrderCreationService.buildResponse(response);
        assertTrue(maybeResponse.isPresent());
        DetailsResponse actual = maybeResponse.get();
        assertAll(
                () -> assertEquals(bank + " " + detailsString, actual.getDetails()),
                () -> assertEquals(Merchant.NICE_PAY, actual.getMerchant()),
                () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(paymentId, actual.getMerchantOrderId())
        );
    }

    @ParameterizedTest
    @CsvSource({
            "Сбербанк",
            "T-BANK"
    })
    void buildResponseShouldBuildResponseObjectWithComment(String bank) {
        Response response = new Response();
        response.setStatus(Response.ResponseStatus.DETAILS_FOUND);
        Response.Data data = new Response.Data();
        response.setData(data);
        data.setStatus(Status.CREATED);
        data.setPaymentId("paymentId");
        Response.Data.Details details = new Response.Data.Details();
        details.setWallet("wallet");
        details.setComment(bank);
        data.setDetails(details);

        Optional<DetailsResponse> maybeResponse = nicePayOrderCreationService.buildResponse(response);
        assertTrue(maybeResponse.isPresent());
        DetailsResponse actual = maybeResponse.get();
        assertAll(
                () -> assertEquals(bank + " wallet", actual.getDetails())
        );
    }

    @Test
    void buildResponseShouldBuildResponseObjectWithJustWallet() {
        Response response = new Response();
        response.setStatus(Response.ResponseStatus.DETAILS_FOUND);
        Response.Data data = new Response.Data();
        response.setData(data);
        data.setStatus(Status.CREATED);
        data.setPaymentId("paymentId");
        Response.Data.Details details = new Response.Data.Details();
        details.setWallet("wallet");
        data.setDetails(details);

        Optional<DetailsResponse> maybeResponse = nicePayOrderCreationService.buildResponse(response);
        assertTrue(maybeResponse.isPresent());
        DetailsResponse actual = maybeResponse.get();
        assertAll(
                () -> assertEquals("wallet", actual.getDetails())
        );
    }
}