package tgb.cryptoexchange.merchantdetails.details.fiatcut;

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
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequestWithMethod;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.FiatCutProperties;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FiatCutOrderCreationServiceTest {

    @Mock
    private CallbackConfig callbackConfig;

    @Mock
    private FiatCutProperties fiatCutProperties;

    @InjectMocks
    private FiatCutOrderCreationService fiatCutOrderCreationService;

    @Test
    void getMerchantShouldReturnFiatCut() {
        assertEquals(Merchant.FIAT_CUT, fiatCutOrderCreationService.getMerchant());
    }

    @Test
    void uriBuilderShouldAddUriPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/api/h2h/order", fiatCutOrderCreationService.uriBuilder(null).apply(uriBuilder).getPath());
    }

    @CsvSource({
            "JQX1BI3Vs36UnMB",
            "y701U9erXYfOAdX",
            "w1vGjx4COVk531JZgj6dsh7uT"
    })
    @ParameterizedTest
    void headersShouldAddRequiredHeaders(String token) {
        when(fiatCutProperties.token()).thenReturn(token);
        HttpHeaders headers = new HttpHeaders();
        fiatCutOrderCreationService.headers(null, null).accept(headers);
        assertAll(
                () -> assertEquals("application/json", Objects.requireNonNull(headers.get("Accept")).getFirst()),
                () -> assertEquals(token, Objects.requireNonNull(headers.get("Access-Token")).getFirst())
        );
    }

    @CsvSource({
            "2100,https://gateway.paysendmmm.online,CARD,xgjUpv5iEOnWKN2,4480909b-89fd-4b23-b170-0fc562ba09d6",
            "2100,https://someaddress.online,PHONE,PfWjesdX49mQSKJ,41579b03-92a3-49fc-9b9a-8eb87b25d795"
    })
    @ParameterizedTest
    void bodyShouldReturnMappedBody(Integer amount, String gatewayUrl, String method, String secret, String merchantId) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setMethods(List.of(DetailsRequest.MerchantMethod.builder().merchant(Merchant.FIAT_CUT).method(Collections.singletonList(method)).build()));
        String expectedCallbackUrl = gatewayUrl + "/merchant-details/callback?merchant=FIAT_CUT&secret=" + secret;
        when(callbackConfig.getGatewayUrl()).thenReturn(gatewayUrl);
        when(callbackConfig.getCallbackSecret()).thenReturn(secret);
        when(fiatCutProperties.merchantId()).thenReturn(merchantId);
        Request request = fiatCutOrderCreationService.body(new DetailsRequestWithMethod(detailsRequest, method));
        assertAll(
                () -> assertDoesNotThrow(() -> UUID.fromString(request.getExternalId())),
                () -> assertEquals(Method.valueOf(method), request.getMethod()),
                () -> assertEquals(amount, request.getAmount()),
                () -> assertEquals(expectedCallbackUrl, request.getCallbackUrl()),
                () -> assertEquals(merchantId, request.getMerchantId())
        );
    }

    @CsvSource({
            "7cc259a2-67a2-4a67-8e7e-e0342c08da81,PENDING,1234123412341234,5001,Альфа-банк",
            "418adf2c-c382-42f4-8bbc-0d0ea008f701,SUCCESS,9876987654325432,1222,Сбербанк"
    })
    @ParameterizedTest
    void buildResponseShouldBuildRequisiteResponse(String id, Status status, String requisiteString, Integer amount, String bank) {
        Response.Data data = new Response.Data();
        data.setOrderId(id);
        data.setStatus(status);
        data.setAmount(amount.toString());
        Response.Data.PaymentDetail paymentDetail = new Response.Data.PaymentDetail();
        data.setBankName(bank);
        paymentDetail.setDetail(requisiteString);
        data.setPaymentDetail(paymentDetail);
        Response response = new Response();
        response.setData(data);
        response.setSuccess(true);

        Optional<DetailsResponse> maybeRequisiteResponse = fiatCutOrderCreationService.buildResponse(response);
        assertTrue(maybeRequisiteResponse.isPresent());
        DetailsResponse actual = maybeRequisiteResponse.get();
        assertAll(
                () -> assertEquals(Merchant.FIAT_CUT, actual.getMerchant()),
                () -> assertEquals(id, actual.getMerchantOrderId()),
                () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(bank + " " + requisiteString, actual.getDetails())
        );
    }

}