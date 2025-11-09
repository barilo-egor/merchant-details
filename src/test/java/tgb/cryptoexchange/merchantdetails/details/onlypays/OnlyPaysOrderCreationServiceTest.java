package tgb.cryptoexchange.merchantdetails.details.onlypays;

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
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.OnlyPaysProperties;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnlyPaysOrderCreationServiceTest {

    @Mock
    private OnlyPaysProperties onlyPaysProperties;

    @InjectMocks
    private OnlyPaysOrderCreationService onlyPaysOrderCreationService;

    @Test
    void getMerchantShouldReturnOnlyPays() {
        assertEquals(Merchant.ONLY_PAYS, onlyPaysOrderCreationService.getMerchant());
    }

    @Test
    void uriBuilderShouldAddPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/get_requisite", onlyPaysOrderCreationService.uriBuilder(null).apply(uriBuilder).getPath());
    }

    @Test
    void headersShouldAddRequiredHeaders() {
        HttpHeaders headers = new HttpHeaders();
        onlyPaysOrderCreationService.headers(null, null).accept(headers);
        assertEquals("application/json", Objects.requireNonNull(headers.get("Content-Type")).getFirst());
    }

    @CsvSource({
            "54324,CARD,oMV6SxyOQRM4j3t,3NJMjh1hu00vztS5rrNxviNMWvUb7SpT",
            "2240,SBP,N1DHXLxiLJN2TqG,nb5eHWOSRdkY3KfpP9jnosRlWTjHzjoR"
    })
    @ParameterizedTest
    void bodyShouldBuildRequestObject(Integer amount, Method method, String id, String secret) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setMethod(method.name());
        when(onlyPaysProperties.id()).thenReturn(id);
        when(onlyPaysProperties.secret()).thenReturn(secret);
        Request actual = onlyPaysOrderCreationService.body(detailsRequest);
        assertAll(
                () -> assertEquals(id, actual.getApiId()),
                () -> assertEquals(amount, actual.getAmount()),
                () -> assertEquals(method, actual.getMethod()),
                () -> assertEquals(secret, actual.getSecretKey()),
                () -> assertDoesNotThrow(() -> UUID.fromString(actual.getPersonalId()))
        );
    }

    @CsvSource({
            "ALFA,79853483212,e60e3369-da6a-469f-97f9-488446ca5c83",
            "Т банк,1234123412341234,05b1e745-02b5-47f2-83f6-23ebcc3886ee"
    })
    @ParameterizedTest
    void buildResponseShouldBuildResponseObject(String bank, String requisites, String id) {
        Response response = new Response();
        Response.Data data = new Response.Data();
        data.setBank(bank);
        data.setRequisite(requisites);
        data.setId(id);
        response.setData(data);
        Optional<DetailsResponse> maybeResponse = onlyPaysOrderCreationService.buildResponse(response);
        assertTrue(maybeResponse.isPresent());
        DetailsResponse actual = maybeResponse.get();
        assertAll(
                () -> assertEquals(Merchant.ONLY_PAYS, actual.getMerchant()),
                () -> assertEquals(bank + " " + requisites, actual.getDetails()),
                () -> assertEquals(id, actual.getMerchantOrderId()),
                () -> assertEquals(Status.WAITING.name(), actual.getMerchantOrderStatus())
        );
    }
}