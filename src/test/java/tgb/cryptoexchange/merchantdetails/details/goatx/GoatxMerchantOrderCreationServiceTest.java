package tgb.cryptoexchange.merchantdetails.details.goatx;

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
import tgb.cryptoexchange.merchantdetails.details.BotDetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.GoatxPropertiesImpl;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoatxMerchantOrderCreationServiceTest {

    @Mock
    private GoatxPropertiesImpl goatxProperties;

    @InjectMocks
    private GoatxMerchantOrderCreationServiceImpl goatxMerchantOrderCreationService;

    @Test
    void getMerchantShouldReturnFiatCut() {
        assertEquals(Merchant.GOAT_X, goatxMerchantOrderCreationService.getMerchant());
    }

    @Test
    void uriBuilderShouldAddUriPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/api/order/", goatxMerchantOrderCreationService.uriBuilder(null, null).apply(uriBuilder).getPath());
    }

    @Test
    void headersShouldAddRequiredHeaders() {
        HttpHeaders headers = new HttpHeaders();
        goatxMerchantOrderCreationService.headers(null, null, null).accept(headers);
        assertAll(
                () -> assertEquals("application/json", Objects.requireNonNull(headers.get("Content-Type")).getFirst())
        );
    }

    @CsvSource(value = {
            "1000,7fd54bb4-f20e-489d-a84a-4ff3490ea19e,SBP",
            "5054,33d29f27-195a-4870-94a4-1ca7af340cb9,CARD"
    })
    @ParameterizedTest
    void bodyShouldReturnMappedBody(Integer amount, String contractId, String method) {
        BotDetailsRequest detailsRequest = spy(new BotDetailsRequest());
        detailsRequest.setAmount(amount);
        when(goatxProperties.merchantContractId()).thenReturn(contractId);

        Request result = goatxMerchantOrderCreationService.body(detailsRequest, method);

        assertAll(
                () -> assertEquals(amount.toString(), result.getSum()),
                () -> assertEquals(contractId, result.getContract()),
                () -> assertNotNull(result.getInvid()),
                () -> assertDoesNotThrow(() -> UUID.fromString(result.getInvid())),
                () -> assertNotNull(result.getSignature()),
                () -> assertEquals(64, result.getSignature().length()),
                () -> assertEquals(Method.valueOf(method), result.getWay())
        );
    }

    @CsvSource({
            "7cc259a2-67a2-4a67-8e7e-e0342c08da81,PENDING,+7814587946,Альфа-банк",
            "418adf2c-c382-42f4-8bbc-0d0ea008f701,CONFIRMED,+78944561234,Сбербанк"
    })
    @ParameterizedTest
    void buildResponseShouldBuildPhoneRequisiteResponse(String id, Status status, String requisiteString, String bankName) {
        Response response = new Response();
        response.setId(id);
        response.setStatus(status);
        response.setWay(Method.SBP);
        Response.Requisite requisite = new Response.Requisite();
        Response.Requisite.Bank bank = new Response.Requisite.Bank();
        bank.setName(bankName);
        requisite.setBank(bank);
        requisite.setPhoneNumber(requisiteString);
        response.setRequisite(requisite);
        Optional<DetailsResponse> maybeActual = goatxMerchantOrderCreationService.buildResponse(response);
        assertTrue(maybeActual.isPresent());
        DetailsResponse actual = maybeActual.get();
        assertAll(
            () -> assertEquals(Merchant.GOAT_X, actual.getMerchant()),
            () -> assertEquals(id, actual.getMerchantOrderId()),
            () -> assertEquals(bankName + " " + requisiteString, actual.getDetails()),
            () -> assertEquals(status.name(), actual.getMerchantOrderStatus())
        );
    }

    @CsvSource({
            "1234 1234 1234 1234,Альфа-банк",
            "9999 9999 9999 9999,Сбербанк"
    })
    @ParameterizedTest
    void buildResponseShouldBuildCardRequisiteResponse(String requisiteString, String bankName) {
        Response response = new Response();
        response.setId("id");
        response.setStatus(Status.PENDING);
        response.setWay(Method.CARD);
        Response.Requisite requisite = new Response.Requisite();
        Response.Requisite.Bank bank = new Response.Requisite.Bank();
        bank.setName(bankName);
        requisite.setBank(bank);
        requisite.setCardNumber(requisiteString);
        response.setRequisite(requisite);
        Optional<DetailsResponse> maybeActual = goatxMerchantOrderCreationService.buildResponse(response);
        assertTrue(maybeActual.isPresent());
        DetailsResponse actual = maybeActual.get();
        assertEquals(bankName + " " + requisiteString, actual.getDetails());
    }
}