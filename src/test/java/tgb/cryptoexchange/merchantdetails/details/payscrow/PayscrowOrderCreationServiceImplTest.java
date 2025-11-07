package tgb.cryptoexchange.merchantdetails.details.payscrow;

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
import tgb.cryptoexchange.merchantdetails.properties.PayscrowPropertiesImpl;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayscrowOrderCreationServiceImplTest {

    @Mock
    private PayscrowPropertiesImpl payscrowProperties;

    @InjectMocks
    private PayscrowOrderCreationServiceImpl payscrowOrderCreationService;

    @Test
    void getMerchantShouldReturnPayscrow() {
        assertEquals(Merchant.PAYSCROW, payscrowOrderCreationService.getMerchant());
    }

    @Test
    void uriBuilderShouldAddPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals(
                "/api/v1/order/",
                payscrowOrderCreationService.uriBuilder(null).apply(uriBuilder).getPath()
        );
    }

    @ValueSource(strings = {
            "PzeiDYNh1RTRD5d", "L76sF2r7uL1ClNF"
    })
    @ParameterizedTest
    void headersShouldAddRequiredHeaders(String key) {
        when(payscrowProperties.key()).thenReturn(key);
        HttpHeaders headers = new HttpHeaders();
        payscrowOrderCreationService.headers(null, null).accept(headers);
        assertAll(
                () -> assertEquals("application/json", headers.getFirst("Content-Type")),
                () -> assertEquals(key, headers.getFirst("X-API-Key"))
        );
    }

    @CsvSource(textBlock = """
            5220,BANK_CARD
            2552,SBP
            """)
    @ParameterizedTest
    void bodyShouldBuildRequestObject(Integer amount, Method method) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setMethod(method.name());
        Request request = payscrowOrderCreationService.body(detailsRequest);
        assertAll(
                () -> assertEquals(amount, request.getAmount()),
                () -> assertEquals(method, request.getPaymentMethod()),
                () -> assertDoesNotThrow(() -> UUID.fromString(request.getClientOrderId()))
        );
    }

    @CsvSource(textBlock = """
            8ab90c56-4a96-4f01-be9a-170a9e8f9d68,UNPAID,Альфа,79877892387
            c1098ddc-ef6c-48c0-bd27-cd0f08abffa4,COMPLETED,SBER,6666555544443333
            """)
    @ParameterizedTest
    void buildResponseShouldBuildResponseObject(String id, Status status, String methodName, String holderAccount) {
        Response response = new Response();
        response.setId(id);
        response.setStatus(status);
        response.setMethodName(methodName);
        response.setHolderAccount(holderAccount);

        Optional<DetailsResponse> detailsResponse = payscrowOrderCreationService.buildResponse(response);
        assertTrue(detailsResponse.isPresent());
        DetailsResponse actual = detailsResponse.get();
        assertAll(
                () -> assertEquals(id, actual.getMerchantOrderId()),
                () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(methodName + " " + holderAccount, actual.getDetails()),
                () -> assertEquals(Merchant.PAYSCROW, actual.getMerchant())
        );
    }
}