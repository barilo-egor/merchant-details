package tgb.cryptoexchange.merchantdetails.details.lotrien;

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
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.LotrienProperties;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LotrienOrderCreationServiceTest {

    @Mock
    private LotrienProperties lotrienProperties;

    @InjectMocks
    private LotrienOrderCreationService service;

    @Test
    void uriBuilderShouldAddPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/order/payin",
                service.uriBuilder(null).apply(uriBuilder).getPath());
    }

    @ValueSource(strings = {
            "JQX1BI3Vs36UnMBC", "y701U9erXYfOAdXC", "k531JZgj6dsh7uTV"
    })
    @ParameterizedTest
    void headersShouldAddRequiredHeaders(String key) {
        when(lotrienProperties.key()).thenReturn(key);
        HttpHeaders headers = new HttpHeaders();
        service.headers(null, null).accept(headers);
        assertAll(
                () -> assertEquals(lotrienProperties.key(), Objects.requireNonNull(headers.get("X-API-Key")).getFirst()),
                () -> assertEquals("application/json", Objects.requireNonNull(headers.get("Content-Type")).getFirst()));
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            5000, BANK_CARD
            10500, SBP
            """)
    void body(String amount, Method method) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(Integer.valueOf(amount));
        detailsRequest.setMethods(
                List.of(DetailsRequest.MerchantMethod.builder().merchant(Merchant.LOTRIEN).method(Collections.singletonList(method.name()))
                        .build()));
        detailsRequest.setCurrentMerchantMethod(method.name());
        Request actual = service.body(detailsRequest);
        assertAll(
                () -> assertEquals(Integer.valueOf(amount), new BigDecimal(actual.getFiatSum()).intValue()),
                () -> assertEquals(method, actual.getPaymentMethod())
        );
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            1000, ID-444, PENDING, Сбербанк, 2200111122223333
            500, ID-555, SUCCESS, Альфа-Банк, 4444555566667777
            """)
    void buildResponseShouldBuildIfCardResponseObject(String amount, String id, Status status,
                                                      String bankName, String cardValue) {
        Response response = new Response();
        response.setPaymentMethod(Method.BANK_CARD);
        response.setId(id);
        response.setStatus(status);
        response.setAmount(amount);

        Response.Requisites requisite = new Response.Requisites();
        requisite.setBank(bankName);
        requisite.setCardNumber(cardValue);
        response.setRequisites(requisite);

        Optional<DetailsResponse> maybeResponse = service.buildResponse(response);

        DetailsResponse actual = maybeResponse.get();

        String expectedDetails = String.format("%s %s", bankName, cardValue);
        assertAll(
                () -> assertEquals(Integer.valueOf(amount), actual.getAmount()),
                () -> assertEquals(id, actual.getMerchantOrderId()),
                () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(expectedDetails, actual.getDetails()),
                () -> assertEquals(service.getMerchant(), actual.getMerchant())
        );
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            1000, ID-444, CREATED, Сбербанк, 88005553535
            500, ID-555, UNISSUED, Альфа-Банк, 88005553535
            """)
    void buildResponseShouldBuildIfPhoneResponseObject(String amount, String id, Status status,
                                                       String bankName, String phoneValue) {
        Response response = new Response();
        response.setPaymentMethod(Method.SBP);
        response.setId(id);
        response.setStatus(status);
        response.setAmount(amount);

        Response.Requisites requisite = new Response.Requisites();
        requisite.setBank(bankName);
        requisite.setPhoneNumber(phoneValue);
        response.setRequisites(requisite);

        Optional<DetailsResponse> maybeResponse = service.buildResponse(response);

        DetailsResponse actual = maybeResponse.get();

        String expectedDetails = String.format("%s %s", bankName, phoneValue);
        assertAll(
                () -> assertEquals(Integer.valueOf(amount), actual.getAmount()),
                () -> assertEquals(id, actual.getMerchantOrderId()),
                () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(expectedDetails, actual.getDetails()),
                () -> assertEquals(service.getMerchant(), actual.getMerchant())
        );
    }

    @Test
    void getMerchantShouldReturnLotrien() {
        assertEquals(Merchant.LOTRIEN, service.getMerchant());
    }

}