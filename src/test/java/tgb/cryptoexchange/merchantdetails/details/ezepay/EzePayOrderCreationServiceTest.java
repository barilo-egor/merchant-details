package tgb.cryptoexchange.merchantdetails.details.ezepay;

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
import tgb.cryptoexchange.merchantdetails.properties.EzePayProperties;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EzePayOrderCreationServiceTest {

    @Mock
    private EzePayProperties ezePayProperties;

    @InjectMocks
    private EzePayOrderCreationService ezePayOrderCreationService;

    @Test
    void getMerchantShouldReturnEzePay() {
        assertEquals(Merchant.EZE_PAY, ezePayOrderCreationService.getMerchant());
    }

    @Test
    void uriBuilderShouldAddPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/createOrder/", ezePayOrderCreationService.uriBuilder(null).apply(uriBuilder).getPath());
    }

    @Test
    void headersShouldAddRequiredHeaders() {
        HttpHeaders headers = new HttpHeaders();
        ezePayOrderCreationService.headers(null, "").accept(headers);
        assertAll(
                () -> assertEquals("application/json", Objects.requireNonNull(headers.get("Content-Type")).getFirst())
        );
    }

    @CsvSource({
            "1002,SBP,144532,y0YTPpz4X0hk0Ou",
            "15220,CARD,122,LnzLBV3p3L8czzF"
    })
    @ParameterizedTest
    void bodyShouldBuildRequestObject(Integer amount, String method, Long id, String key) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setMethod(method);
        when(ezePayProperties.id()).thenReturn(id.toString());
        when(ezePayProperties.key()).thenReturn(key);
        Request actual = ezePayOrderCreationService.body(detailsRequest);
        assertAll(
                () -> assertEquals(actual.getAmount(), amount),
                () -> assertEquals(actual.getBank(), Method.valueOf(method).getId()),
                () -> assertDoesNotThrow(() -> UUID.fromString(actual.getOrder())),
                () -> assertEquals(id, actual.getShopId()),
                () -> assertEquals(key, actual.getKey())
        );
    }

    @CsvSource({
            "e356e472-1837-4fe3-9a7a-c0fd445663df,ALFA,1111222233334444",
            "784fd85f-48a9-4521-8d3d-a2bef09b98ba,Сбербанк,1234432112344321"
    })
    @ParameterizedTest
    void buildResponseShouldBuildResponseObjectWithBank(String orderId, String bank, String details) {
        Response response = new Response();
        response.setStatus("success");
        Response.Data data = new Response.Data();
        data.setOrderId(orderId);
        data.setBank(bank);
        data.setDetails(details);
        response.setData(data);

        Optional<DetailsResponse> maybeDetailsResponse = ezePayOrderCreationService.buildResponse(response);
        assertTrue(maybeDetailsResponse.isPresent());
        DetailsResponse actual = maybeDetailsResponse.get();
        assertAll(
                () -> assertEquals(Merchant.EZE_PAY, actual.getMerchant()),
                () -> assertEquals(orderId, actual.getMerchantOrderId()),
                () -> assertEquals(Status.CHOOSING_METHOD.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(bank + " " + details, actual.getDetails())
        );
    }

    @ValueSource(strings = {
            "ALFA", "Сбербанк"
    })
    @ParameterizedTest
    void buildResponseShouldBuildResponseObjectWithBankSbp(String bankSbp) {
        Response response = new Response();
        response.setStatus("success");
        Response.Data data = new Response.Data();
        data.setOrderId("orderId");
        data.setBankSbp(bankSbp);
        data.setDetails("details");
        response.setData(data);

        Optional<DetailsResponse> maybeDetailsResponse = ezePayOrderCreationService.buildResponse(response);
        assertTrue(maybeDetailsResponse.isPresent());
        DetailsResponse actual = maybeDetailsResponse.get();
        assertAll(
                () -> assertEquals(bankSbp + " details", actual.getDetails())
        );
    }
}