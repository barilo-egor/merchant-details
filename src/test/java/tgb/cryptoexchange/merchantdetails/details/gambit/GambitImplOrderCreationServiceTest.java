package tgb.cryptoexchange.merchantdetails.details.gambit;

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
import tgb.cryptoexchange.merchantdetails.properties.GambitImplProperties;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GambitImplOrderCreationServiceTest {

    @Mock
    private GambitImplProperties gambitProperties;

    @InjectMocks
    private GambitImplOrderCreationService service;

    @Test
    void uriBuilderShouldAddPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/orders/init",
                service.uriBuilder(null).apply(uriBuilder).getPath());
    }

    @ValueSource(strings = {
            "JQX1BI3Vs36UnMBC", "y701U9erXYfOAdXC", "k531JZgj6dsh7uTV"
    })
    @ParameterizedTest
    void headersShouldAddRequiredHeaders(String key) {
        when(gambitProperties.key()).thenReturn(key);
        HttpHeaders headers = new HttpHeaders();
        service.headers(null, null).accept(headers);
        assertAll(
                () -> assertEquals("Bearer " + gambitProperties.key(), Objects.requireNonNull(headers.get("Authorization")).getFirst()),
                () -> assertEquals("application/json", Objects.requireNonNull(headers.get("Content-Type")).getFirst()));
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            5000, CARD
            10500, SBP
            """)
    void body(String amount, Method method) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(Integer.valueOf(amount));
        detailsRequest.setMethods(
                List.of(DetailsRequest.MerchantMethod.builder().merchant(Merchant.GAMBIT).method(Collections.singletonList(method.name()))
                        .build()));
        detailsRequest.setCurrentMerchantMethod(method.name());
        Request actual = service.body(detailsRequest);
        assertAll(
                () -> assertEquals(Integer.valueOf(amount), new BigDecimal(actual.getAmount()).intValue())
        );
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            1000.0, ID-444, SUCCEEDED, Сбербанк, 2200111122223333
            500.0, ID-555, CANCELLED, Альфа-Банк, 4444555566667777
            """)
    void buildResponseShouldBuildIfCardResponseObject(Double amount, String id, Status status,
                                                      String bankName, String cardValue) {
        Response response = new Response();
        response.setId(id);
        response.setStatus(status);
        response.setAmount(amount);

        Response.Requisites requisite = new Response.Requisites();
        requisite.setBankName(bankName);
        requisite.setCardNumber(cardValue);
        response.setPaymentDetails(requisite);

        Optional<DetailsResponse> maybeResponse = service.buildResponse(response);

        DetailsResponse actual = maybeResponse.get();

        String expectedDetails = String.format("%s %s", bankName, cardValue);
        assertAll(
                () -> assertEquals(amount.intValue(), actual.getAmount()),
                () -> assertEquals(id, actual.getMerchantOrderId()),
                () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(expectedDetails, actual.getDetails()),
                () -> assertEquals(service.getMerchant(), actual.getMerchant())
        );
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            1000.0, ID-444, SUCCEEDED, Сбербанк, 88005553535
            500.0, ID-555, CANCELLED, Альфа-Банк, 88005553535
            """)
    void buildResponseShouldBuildIfPhoneResponseObject(Double amount, String id, Status status,
                                                       String bankName, String phoneValue) {
        Response response = new Response();
        response.setId(id);
        response.setStatus(status);
        response.setAmount(amount);

        Response.Requisites requisite = new Response.Requisites();
        requisite.setBankName(bankName);
        requisite.setPhone(phoneValue);
        response.setPaymentDetails(requisite);

        Optional<DetailsResponse> maybeResponse = service.buildResponse(response);

        DetailsResponse actual = maybeResponse.get();

        String expectedDetails = String.format("%s %s", bankName, phoneValue);
        assertAll(
                () -> assertEquals(amount.intValue(), actual.getAmount()),
                () -> assertEquals(id, actual.getMerchantOrderId()),
                () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(expectedDetails, actual.getDetails()),
                () -> assertEquals(service.getMerchant(), actual.getMerchant())
        );
    }

    @Test
    void getMerchantShouldReturnGambit() {
        assertEquals(Merchant.GAMBIT, service.getMerchant());
    }

}