package tgb.cryptoexchange.merchantdetails.details.paylee;

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
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.PayLeeProperties;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayLeeMerchantServiceTest {

    @Mock
    private PayLeeProperties payLeeProperties;

    @InjectMocks
    private PayLeeMerchantService payLeeMerchantService;

    @Test
    void getMerchantShouldReturnPayLee() {
        assertEquals(Merchant.PAY_LEE, payLeeMerchantService.getMerchant());
    }

    @Test
    void uriBuilderShouldAddPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/partners/purchases/", payLeeMerchantService.uriBuilder(null).apply(uriBuilder).getPath());
    }

    @ValueSource(strings = {
            "PzeiDYNh1RTRD5d", "L76sF2r7uL1ClNF"
    })
    @ParameterizedTest
    void headersShouldAddRequiredHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        when(payLeeProperties.token()).thenReturn(token);
        payLeeMerchantService.headers(null, null).accept(headers);
        assertEquals("Token " + token, headers.getFirst(HttpHeaders.AUTHORIZATION));
    }

    @CsvSource(textBlock = """
            5600,CARD
            2504,SBP
            """)
    @ParameterizedTest
    void bodyShouldBuildRequestObject(Integer amount, Method method) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setMethod(method.name());

        Request actual = payLeeMerchantService.body(detailsRequest);
        assertAll(
            () -> assertEquals(amount, actual.getPrice()),
            () -> assertEquals(method, actual.getRequisiteType())
        );
    }

    @CsvSource(textBlock = """
            Альфа банк,1234123412341234,55003,PENDING,55004
            Тинькофф,79869869898,2400,COMPLETED,2399
            """)
    @ParameterizedTest
    void buildResponseShouldBuildResponseObject(String bankName, String requisites, Integer id, Status status, Double price) {
        Response response = new Response();
        response.setBankName(bankName);
        response.setRequisites(requisites);
        response.setId(id);
        response.setStatus(status);
        response.setPrice(price);

        Optional<DetailsResponse> maybeResponse = payLeeMerchantService.buildResponse(response);
        assertTrue(maybeResponse.isPresent());
        DetailsResponse actual = maybeResponse.get();
        assertAll(
            () -> assertEquals(bankName + " " + requisites, actual.getDetails()),
            () -> assertEquals(Merchant.PAY_LEE, actual.getMerchant()),
            () -> assertEquals(id.toString(), actual.getMerchantOrderId()),
            () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
            () -> assertEquals(price.intValue(), actual.getAmount())
        );
    }
}