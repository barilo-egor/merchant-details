package tgb.cryptoexchange.merchantdetails.details.pspware;

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
import tgb.cryptoexchange.merchantdetails.constants.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.PspWareProperties;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PspWareOrderCreationServiceTest {

    @Mock
    private PspWareProperties pspWareProperties;

    @InjectMocks
    private PspWareOrderCreationService pspWareOrderCreationService;

    @Test
    void getMerchantShouldReturnPspWare() {
        assertEquals(Merchant.PSP_WARE, pspWareOrderCreationService.getMerchant());
    }

    @Test
    void uriBuilderShouldAddPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/merchant/v2/orders", pspWareOrderCreationService.uriBuilder(null).apply(uriBuilder).getPath());
    }

    @ValueSource(strings = {
            "PzeiDYNh1RTRD5d", "L76sF2r7uL1ClNF"
    })
    @ParameterizedTest
    void headersShouldAddRequiredHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        when(pspWareProperties.token()).thenReturn(token);
        Consumer<HttpHeaders> headersConsumer = pspWareOrderCreationService.headers(null, null);
        headersConsumer.accept(headers);
        assertEquals(token, headers.getFirst("X-API-KEY"));
    }

    @ValueSource(ints = {2550, 6340})
    @ParameterizedTest
    void bodyShouldReturnRequestObjectWithTJKGeo(Integer amount) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setMethods(List.of(DetailsRequest.MerchantMethod.builder().merchant(Merchant.PSP_WARE).method(Method.TRANSGRAN_PHONE.name()).build()));

        Request actual = pspWareOrderCreationService.body(detailsRequest);
        assertEquals(1, actual.getGeos().size());
        assertEquals(1, actual.getPayTypes().size());
        assertAll(
                () -> assertEquals(amount, actual.getSum()),
                () -> assertEquals("TJK", actual.getGeos().getFirst()),
                () -> assertEquals(Method.TRANSGRAN_PHONE, actual.getPayTypes().getFirst())
        );
    }

    @ValueSource(ints = {2550, 6340})
    @ParameterizedTest
    void bodyShouldReturnRequestObjectWithRUAndABHGeo(Integer amount) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setMethods(List.of(DetailsRequest.MerchantMethod.builder().merchant(Merchant.PSP_WARE).method(Method.SBP.name()).build()));

        Request actual = pspWareOrderCreationService.body(detailsRequest);
        assertEquals(2, actual.getGeos().size());
        assertAll(
                () -> assertEquals("RU", actual.getGeos().getFirst()),
                () -> assertEquals("ABH", actual.getGeos().get(1))
        );
    }

    @ValueSource(ints = {2550, 6340})
    @ParameterizedTest
    void bodyShouldReturnRequestObjectWithRUGeo(Integer amount) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setMethods(List.of(DetailsRequest.MerchantMethod.builder().merchant(Merchant.PSP_WARE).method(Method.CARD.name()).build()));

        Request actual = pspWareOrderCreationService.body(detailsRequest);
        assertEquals(1, actual.getGeos().size());
        assertAll(
                () -> assertEquals("RU", actual.getGeos().getFirst())
                );
    }

    @CsvSource(textBlock = """
            AFLA,1234123412341234,8ab90c56-4a96-4f01-be9a-170a9e8f9d68,PROCESSING
            СБЕР,76544563212,c1098ddc-ef6c-48c0-bd27-cd0f08abffa4,APPEL
            """)
    @ParameterizedTest
    void buildResponseShouldBuildResponseObject(String bankName, String card, String id, Status status) {
        Response response = new Response();
        response.setBankName(bankName);
        response.setCard(card);
        response.setId(id);
        response.setStatus(status);

        Optional<DetailsResponse> maybeResponse = pspWareOrderCreationService.buildResponse(response);
        assertTrue(maybeResponse.isPresent());
        DetailsResponse actual = maybeResponse.get();
        assertAll(
            () -> assertEquals(bankName + " " + card, actual.getDetails()),
            () -> assertEquals(Merchant.PSP_WARE, actual.getMerchant()),
            () -> assertEquals(id, actual.getMerchantOrderId()),
            () -> assertEquals(status.name(), actual.getMerchantOrderStatus())
        );
    }
}