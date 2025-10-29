package tgb.cryptoexchange.merchantdetails.details.appexbit;

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
import tgb.cryptoexchange.merchantdetails.properties.AppexbitProperties;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppexbitOrderCreationServiceTest {

    @Mock
    private AppexbitProperties appexbitProperties;

    @InjectMocks
    private AppexbitOrderCreationService appexbitOrderCreationService;

    @Test
    void getMerchantShouldReturnAppexbitMerchant() {
        assertEquals(Merchant.APPEXBIT, appexbitOrderCreationService.getMerchant());
    }

    @Test
    void uriBuilderShouldAddUriPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/trade/createOffer", appexbitOrderCreationService.uriBuilder().apply(uriBuilder).getPath());
    }

    @ValueSource(strings = {
            "JQX1BI3Vs36UnMB", "y701U9erXYfOAdX", "k531JZgj6dsh7uT"
    })
    @ParameterizedTest
    void headersShouldAddRequiredHeaders(String key) {
        when(appexbitProperties.key()).thenReturn(key);
        HttpHeaders headers = new HttpHeaders();
        appexbitOrderCreationService.headers(null, "").accept(headers);
        assertAll(
                () -> assertEquals(key, Objects.requireNonNull(headers.get("x-api-key")).getFirst()),
                () -> assertEquals("application/json", Objects.requireNonNull(headers.get("Content-Type")).getFirst())
        );
    }

    @CsvSource({
            "2100,https://gateway.paysendmmm.online/merchant/appexbit,CARD",
            "2100,https://someaddress.online/merchant/appexbit,SBP"
    })
    @ParameterizedTest
    void bodyShouldReturnMappedBody(Integer amount, String callbackUrl, String method) {
        RequisiteRequest requisiteRequest = new RequisiteRequest();
        requisiteRequest.setAmount(amount);
        requisiteRequest.setCallbackUrl(callbackUrl);
        requisiteRequest.setMethod(method);
        Request request = (Request) appexbitOrderCreationService.body(requisiteRequest);
        assertAll(
                () -> assertEquals(amount.toString(), request.getAmountFiat()),
                () -> assertEquals(callbackUrl, request.getGoodReturnLink()),
                () -> assertEquals(callbackUrl, request.getBadReturnLink()),
                () -> assertEquals(Method.valueOf(method), request.getPaymentMethod()),
                () -> assertEquals("USDT", request.getTokenCode()),
                () -> assertEquals("RUB", request.getFiatInfo().getFiatCode())
        );
    }

    @CsvSource({
            "23a48cbd-4e38-48b8-81f3-2b64b507738b,ACCEPTED,https://appexbit.com/pay",
            "60e6d22c-1b74-42fe-acab-79a0558afd38,VERIFICATION,1234123412341234"
    })
    @ParameterizedTest
    void buildResponseShouldMapFromResponse(String id, Status status, String message) {
        Response response = new Response();
        response.setSuccess(true);
        Response.Offer offer = new Response.Offer();
        offer.setStatus(status);
        offer.setId(id);
        offer.setMessage(message);
        response.setAddedOffers(List.of(offer));

        Optional<RequisiteResponse> maybeActual = appexbitOrderCreationService.buildResponse(response);
        assertTrue(maybeActual.isPresent());
        RequisiteResponse actual = maybeActual.get();
        assertAll(
                () -> assertNull(actual.getAmount()),
                () -> assertEquals(Merchant.APPEXBIT, actual.getMerchant()),
                () -> assertEquals(id, actual.getMerchantOrderId()),
                () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(message, actual.getRequisite())
        );
    }

    @Test
    void buildResponseShouldReturnEmptyOptionalIfSuccessIsNull() {
        Response response = new Response();
        assertTrue(appexbitOrderCreationService.buildResponse(response).isEmpty());
    }

    @Test
    void buildResponseShouldReturnEmptyOptionalIfSuccessIsFalse() {
        Response response = new Response();
        response.setSuccess(false);
        assertTrue(appexbitOrderCreationService.buildResponse(response).isEmpty());
    }

    @Test
    void buildResponseShouldReturnEmptyOptionalIfAddedOfferIsNull() {
        Response response = new Response();
        response.setSuccess(true);
        assertTrue(appexbitOrderCreationService.buildResponse(response).isEmpty());
    }

    @Test
    void buildResponseShouldReturnEmptyOptionalIfAddedOfferIsEmpty() {
        Response response = new Response();
        response.setSuccess(true);
        response.setAddedOffers(List.of());
        assertTrue(appexbitOrderCreationService.buildResponse(response).isEmpty());
    }
}