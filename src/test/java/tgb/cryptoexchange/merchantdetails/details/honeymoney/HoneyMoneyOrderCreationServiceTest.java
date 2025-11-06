package tgb.cryptoexchange.merchantdetails.details.honeymoney;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
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
import tgb.cryptoexchange.merchantdetails.properties.HoneyMoneyProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HoneyMoneyOrderCreationServiceTest {

    @Mock
    private HoneyMoneyProperties honeyMoneyProperties;

    @Mock
    private SignatureService signatureService;

    @InjectMocks
    private HoneyMoneyOrderCreationService honeyMoneyOrderCreationService;

    @Test
    void getMerchantShouldReturnHoneyMoney() {
        assertEquals(Merchant.HONEY_MONEY, honeyMoneyOrderCreationService.getMerchant());
    }

    @EnumSource(Method.class)
    @ParameterizedTest
    void uriBuilderShouldAddPathDependsOnMethod(Method method) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setMethod(method.name());
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals(method.getUri(), honeyMoneyOrderCreationService.uriBuilder(detailsRequest).apply(uriBuilder).getPath());
    }

    @CsvSource({
            "iXyHJ2zMDavNqGI,873adbf2fe21f57428aed05465a5f09644055d240099397418f41bc59652a13e",
            "nDHgf5OQX0h5baF,7ddc9f1ebaace1bb7972a0e2ae79263108664f23a11da53ab585ed26dfeb9d4a"
    })
    @ParameterizedTest
    void headersShouldAddRequiredHeaders(String authToken, String signature) {
        when(honeyMoneyProperties.authToken()).thenReturn(authToken);
        when(signatureService.hmacSHA256(any(), any(), any())).thenReturn(signature);

        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setMethod(Method.CARD.name());
        HttpHeaders headers = new HttpHeaders();
        honeyMoneyOrderCreationService.headers(detailsRequest, "body").accept(headers);
        assertAll(
                () -> assertEquals("Bearer " + authToken, Objects.requireNonNull(headers.get("Authorization")).getFirst()),
                () -> assertEquals("application/json", Objects.requireNonNull(headers.get("Content-Type")).getFirst()),
                () -> assertEquals(signature, Objects.requireNonNull(headers.get("X-Signature")).getFirst())
        );
    }

    @CsvSource({
            "12500,CARD,https://gateway.paysendmmm.online/merchant/honeymoney",
            "2566,SBP,https://cryptoexchange.com/honeymoney/callback"
    })
    @ParameterizedTest
    void bodyShouldBuildRequestObject(Integer amount, Method method, String callbackUrl) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setMethod(method.name());
        detailsRequest.setCallbackUrl(callbackUrl);
        Request request = honeyMoneyOrderCreationService.body(detailsRequest);
        assertAll(
                () -> assertEquals(amount, request.getAmount()),
                () -> assertEquals(method.getBank(), request.getBank()),
                () -> assertEquals(callbackUrl, request.getCallbackUrl()),
                () -> assertDoesNotThrow(() -> UUID.fromString(request.getExtId())),
                () -> assertEquals("RUB", request.getCurrency())
        );
    }

    @CsvSource({
            "79876543223,ALFA,24444",
            "79876543223,T-bank,124"
    })
    @ParameterizedTest
    void buildResponseShouldBuildResponseObjectWithPhoneNumber(String phoneNumber, String bankName, Integer id) {
        Response response = new Response();
        response.setId(id);
        response.setPhoneNumber(phoneNumber);
        response.setBankName(bankName);
        Optional<DetailsResponse> maybeResponse = honeyMoneyOrderCreationService.buildResponse(response);
        assertTrue(maybeResponse.isPresent());
        DetailsResponse actual = maybeResponse.get();
        assertAll(
                () -> assertEquals(Merchant.HONEY_MONEY, actual.getMerchant()),
                () -> assertEquals(bankName + " " + phoneNumber, actual.getDetails()),
                () -> assertEquals(id.toString(), actual.getMerchantOrderId()),
                () -> assertEquals(Status.PENDING.name(), actual.getMerchantOrderStatus())
        );
    }

    @ValueSource(strings = {
            "1111222233334444",
            "1234123412341234"
    })
    @ParameterizedTest
    void buildResponseShouldBuildResponseObjectWithPhoneNumber(String cardNumber) {
        Response response = new Response();
        response.setId(1);
        response.setCardNumber(cardNumber);
        response.setBankName("bankName");
        Optional<DetailsResponse> maybeResponse = honeyMoneyOrderCreationService.buildResponse(response);
        assertTrue(maybeResponse.isPresent());
        DetailsResponse actual = maybeResponse.get();
        assertAll(
                () -> assertEquals("bankName " + cardNumber, actual.getDetails())
        );
    }
}