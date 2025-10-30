package tgb.cryptoexchange.merchantdetails.details.paybox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.merchantdetails.details.RequisiteRequest;
import tgb.cryptoexchange.merchantdetails.details.RequisiteResponse;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.ExtasyPayProperties;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExtasyPayOrderCreationServiceTest {

    @Mock
    private ExtasyPayProperties extasyPayProperties;

    @InjectMocks
    private ExtasyPayOrderCreationService extasyPayOrderCreationService;

    @Test
    void getMerchantShouldReturnExtasyPay() {
        assertEquals(Merchant.EXTASY_PAY, extasyPayOrderCreationService.getMerchant());
    }

    @EnumSource(Method.class)
    @ParameterizedTest
    void uriBuilderShouldSetPathDependsOnMethod(Method method) {
        RequisiteRequest requisiteRequest = new RequisiteRequest();
        requisiteRequest.setMethod(method.name());
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();

        assertEquals(method.getUri(), extasyPayOrderCreationService.uriBuilder(requisiteRequest).apply(uriBuilder).getPath());
    }

    @ValueSource(strings = {
            "ixoQTh3Gf8mY3ik", "6pm5Px76Dyt5VXW65DjnlFgFnwO0f5II"
    })
    @ParameterizedTest
    void headersShouldSetRequiredHeaders(String token) {
        when(extasyPayProperties.token()).thenReturn(token);
        HttpHeaders headers = new HttpHeaders();
        extasyPayOrderCreationService.headers(null, null).accept(headers);
        assertAll(
                () -> assertEquals("Bearer " + token, Objects.requireNonNull(headers.get("Authorization")).getFirst()),
                () -> assertEquals("application/json", Objects.requireNonNull(headers.get("Content-Type")).getFirst())
        );
    }

    @ValueSource(ints = {
            1211, 5004, 522
    })
    @ParameterizedTest
    void bodyShouldBuildRequestObject(int amount) {
        RequisiteRequest requisiteRequest = new RequisiteRequest();
        requisiteRequest.setAmount(amount);

        Request actual = extasyPayOrderCreationService.body(requisiteRequest);

        assertAll(
                () -> assertEquals(amount, actual.getAmount()),
                () -> assertDoesNotThrow(() -> UUID.fromString(actual.getMerchantTransactionId()))
        );
    }

    @CsvSource({
            "144008,1234123412341234,PAID,Статус Банк",
            "1533,9876543212345678,OVERPAID,ALFA"
    })
    @ParameterizedTest
    void getRequisiteResponseShouldBuildRequisiteResponseObjectWithCardNumber(Long id, String requisiteString, Status status, String bank) {
        Response response = new Response();
        response.setId(id);
        response.setStatus(status);
        response.setCardNumber(requisiteString);
        response.setBankName(bank);

        Optional<RequisiteResponse> maybeRequisiteResponse = extasyPayOrderCreationService.buildResponse(response);
        assertTrue(maybeRequisiteResponse.isPresent());
        RequisiteResponse requisiteResponse = maybeRequisiteResponse.get();
        assertAll(
                () -> assertEquals(Merchant.EXTASY_PAY, requisiteResponse.getMerchant()),
                () -> assertEquals(id.toString(), requisiteResponse.getMerchantOrderId()),
                () -> assertEquals(bank + " " + requisiteString, requisiteResponse.getRequisite()),
                () -> assertEquals(status.name(), requisiteResponse.getMerchantOrderStatus())
        );
    }

    @CsvSource({
            "1234123412341234",
            "9876543212345678"
    })
    @ParameterizedTest
    void getRequisiteResponseShouldBuildRequisiteResponseObjectWithPhoneNumber(String requisiteString) {
        Response response = new Response();
        response.setId(1L);
        response.setStatus(Status.ERROR);
        response.setPhoneNumber(requisiteString);
        response.setBankName("bank");

        Optional<RequisiteResponse> maybeRequisiteResponse = extasyPayOrderCreationService.buildResponse(response);
        assertTrue(maybeRequisiteResponse.isPresent());
        RequisiteResponse requisiteResponse = maybeRequisiteResponse.get();
        assertAll(
                () -> assertEquals("bank" + " " + requisiteString, requisiteResponse.getRequisite())
        );
    }

    @Test
    void getRequisiteResponseShouldReturnEmptyOptionalIfResponseHasErrors() {
        Response response = Mockito.mock(Response.class);
        when(response.hasErrors()).thenReturn(true);
        assertTrue(extasyPayOrderCreationService.buildResponse(response).isEmpty());
    }

    @Test
    void getRequisiteResponseShouldReturnEmptyOptionalIfResponseHasNoBankName() {
        Response response = Mockito.mock(Response.class);
        when(response.hasErrors()).thenReturn(false);
        assertTrue(extasyPayOrderCreationService.buildResponse(response).isEmpty());
    }

    @Test
    void getRequisiteResponseShouldReturnEmptyOptionalIfResponseHasNoCardAndPhone() {
        Response response = Mockito.mock(Response.class);
        when(response.hasErrors()).thenReturn(false);
        when(response.getBankName()).thenReturn("bank");
        assertTrue(extasyPayOrderCreationService.buildResponse(response).isEmpty());
    }
}