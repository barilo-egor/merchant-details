package net.rcetech.md.integrations.platform.paybox;

import net.rcetech.md.integrations.platform.CancelOrderRequest;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.integrations.properties.BucksPaySimProperties;
import net.rcetech.md.integrations.service.RequestService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BucksPaySimOrderCreationServiceTest {

    @Mock
    private BucksPaySimProperties bucksPaySimProperties;

    @Mock
    private RequestService requestService;

    @Mock
    private RestClient restClient;

    @InjectMocks
    private BucksPaySimOrderCreationService bucksPaySimOrderCreationService;

    @Captor
    private ArgumentCaptor<Function<UriBuilder, URI>> uriBuilderCaptor;

    @Test
    void getMerchantShouldReturnBucksPaySim() {
        assertEquals(Merchant.BUCKS_PAY_SIM, bucksPaySimOrderCreationService.getMerchant());
    }

    @EnumSource(Method.class)
    @ParameterizedTest
    void uriBuilderShouldSetPathDependsOnMethod(Method method) {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setMethod(method.name());
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();

        assertEquals("/api/v1/transactions" + method.getUri(),
                bucksPaySimOrderCreationService.uriBuilder(detailsRequest).apply(uriBuilder).getPath());
    }

    @ValueSource(strings = {
            "ixoQTh3Gf8mY3ik", "6pm5Px76Dyt5VXW65DjnlFgFnwO0f5II"
    })
    @ParameterizedTest
    void headersShouldSetRequiredHeaders(String token) {
        when(bucksPaySimProperties.token()).thenReturn(token);
        HttpHeaders headers = new HttpHeaders();
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setMethod(Method.CARD.name());
        bucksPaySimOrderCreationService.headers(detailsRequest, null).accept(headers);
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
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setMethod(Method.SBP.name());
        Request actual = bucksPaySimOrderCreationService.body(detailsRequest);

        assertAll(
                () -> assertEquals(amount, actual.getAmount()),
                () -> assertEquals("RUB", actual.getCurrency()),
                () -> assertDoesNotThrow(() -> UUID.fromString(actual.getMerchantTransactionId()))
        );
    }

    @CsvSource({
            "144008,1234123412341234,Статус Банк",
            "1533,9876543212345678,ALFA"
    })
    @ParameterizedTest
    void buildResponseShouldBuildRequisiteResponseObjectWithCardNumber(Long id, String requisiteString, String bank) {
        Response response = new Response();
        response.setId(id);
        response.setCardNumber(requisiteString);
        response.setBankName(bank);

        Optional<DetailsResponse> maybeRequisiteResponse = bucksPaySimOrderCreationService.buildResponse(response);
        assertTrue(maybeRequisiteResponse.isPresent());
        DetailsResponse detailsResponse = maybeRequisiteResponse.get();
        assertAll(
                () -> assertEquals(Merchant.BUCKS_PAY_SIM, detailsResponse.getMerchant()),
                () -> assertEquals(id.toString(), detailsResponse.getMerchantOrderId()),
                () -> assertEquals(bank, detailsResponse.getBank()),
                () -> assertEquals(requisiteString, detailsResponse.getDetails()),
                () -> assertEquals(Status.PROCESS.name(), detailsResponse.getMerchantOrderStatus())
        );
    }

    @CsvSource("""
            4be41169-2786-48f3-98b9-002a23417c45,CARD
            578f16e0-5941-4330-80c3-b2d22ef302b8,SBP
            """)
    @ParameterizedTest
    void makeCancelRequestShouldMakeRequest(String orderId, Method method) {
        bucksPaySimOrderCreationService.setRequestService(requestService);
        CancelOrderRequest cancelOrderRequest = new CancelOrderRequest();
        cancelOrderRequest.setOrderId(orderId);
        cancelOrderRequest.setMethod(method.name());
        bucksPaySimOrderCreationService.makeCancelRequest(cancelOrderRequest);
        verify(requestService).request(eq(restClient), eq(HttpMethod.POST), uriBuilderCaptor.capture(),
                any(), eq(null));
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/api/v1/transactions/" + orderId + "/cancel",
                uriBuilderCaptor.getValue().apply(uriBuilder).getPath());
    }

}
