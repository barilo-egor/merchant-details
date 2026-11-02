package tgb.cryptoexchange.merchantdetails.detailsapi.service;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantService;
import tgb.cryptoexchange.merchantdetails.details.MerchantServiceRegistry;
import tgb.cryptoexchange.merchantdetails.details.OrderCreationRequest;
import tgb.cryptoexchange.merchantdetails.detailsapi.dto.ApiDetailsRequest;
import tgb.cryptoexchange.merchantdetails.detailsapi.dto.ApiDetailsResponse;
import tgb.cryptoexchange.merchantdetails.detailsapi.enums.RequestMethod;
import tgb.cryptoexchange.merchantdetails.entity.ApiMerchantConfig;
import tgb.cryptoexchange.merchantdetails.kafka.MerchantDetailsReceiveEventProducer;
import tgb.cryptoexchange.merchantdetails.service.ApiMerchantConfigService;
import tgb.cryptoexchange.merchantdetails.service.RequestMethodService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Проверяет, что мд отдаёт processing реквизиты мерчанта в ожидаемом формате.
 */
@ExtendWith(MockitoExtension.class)
class ApiMerchantDetailsServiceTest {

    private static final UUID OWNER_ID = UUID.fromString("13afb6d4-6e61-4555-a0ae-072c6407ddb5");

    @Mock
    private ApiMerchantConfigService merchantConfigService;

    @Mock
    private MerchantServiceRegistry merchantServiceRegistry;

    @Mock
    private RequestMethodService requestMethodService;

    @Mock
    private MerchantDetailsReceiveEventProducer merchantDetailsReceiveEventProducer;

    @Mock
    private MerchantService onlyPays;

    @Mock
    private MerchantService evoPay;

    private ApiMerchantDetailsService service;

    @BeforeEach
    void setUp() {
        service = new ApiMerchantDetailsService(new SimpleMeterRegistry(), merchantConfigService,
                merchantServiceRegistry, requestMethodService, merchantDetailsReceiveEventProducer);
    }

    private ApiDetailsRequest request(String userId, RequestMethod... methods) {
        ApiDetailsRequest request = new ApiDetailsRequest();
        request.setInternalId("6f1c2b9e-3d4a-4b8e-9a2f-1e7d5c3b2a10");
        request.setAmount(2000);
        request.setUserId(userId);
        request.setWaitTimeout(1);
        request.setRequestMethods(List.of(methods));
        request.setOwnerId(OWNER_ID);
        return request;
    }

    private DetailsResponse merchantResponse(Merchant merchant, String bank, Integer amount) {
        DetailsResponse response = new DetailsResponse();
        response.setMerchant(merchant);
        response.setMerchantOrderId("m-order-1");
        response.setMerchantOrderStatus("PENDING");
        response.setDetails("2200 0000 0000 0000");
        response.setBank(bank);
        response.setAmount(amount);
        return response;
    }

    private void givenMerchant(MerchantService merchantService, Merchant merchant, String merchantMethod) {
        when(merchantServiceRegistry.getService(merchant)).thenReturn(Optional.of(merchantService));
        when(requestMethodService.getMerchantMethods(eq(merchant), anyList())).thenReturn(List.of(merchantMethod));
    }

    private void givenTurnedOn(Merchant... merchants) {
        when(requestMethodService.getMerchants(anyList())).thenReturn(Set.of(merchants));
        when(merchantConfigService.findAllTurnedByMerchantsAndOwnerId(any(), eq(OWNER_ID))).thenReturn(
                Arrays.stream(merchants)
                        .map(merchant -> ApiMerchantConfig.builder().merchant(merchant).isOn(true).build())
                        .toList());
    }

    private void assertProcessingContract(ApiDetailsResponse response, ApiDetailsRequest request) {
        assertAll(
                () -> assertDoesNotThrow(() -> Merchant.valueOf(response.getMerchant())),
                () -> assertFalse(response.getOrderId() == null || response.getOrderId().isBlank()),
                () -> assertNotNull(response.getDetails()),
                () -> assertTrue(request.getRequestMethods().contains(response.getDetails().getRequestMethod())),
                () -> assertFalse(response.getDetails().getDetails() == null
                        || response.getDetails().getDetails().isBlank()),
                () -> assertFalse(response.getDetails().getBank() == null
                        || response.getDetails().getBank().isBlank()),
                () -> assertTrue(response.getAmount() == null || response.getAmount() > 0)
        );
    }

    @Test
    void getDetailsShouldReturnDetailsMatchingProcessingContract() {
        givenMerchant(onlyPays, Merchant.ONLY_PAYS, "SBP_ONLY_PAYS");
        when(onlyPays.createOrder(any())).thenReturn(Optional.of(merchantResponse(Merchant.ONLY_PAYS, "Сбербанк", 2003)));
        ApiDetailsRequest request = request("demo-user-1", RequestMethod.SBP);

        ApiDetailsResponse actual = service.getDetails(Merchant.ONLY_PAYS, request).orElseThrow();

        assertProcessingContract(actual, request);
        assertAll(
                () -> assertEquals("ONLY_PAYS", actual.getMerchant()),
                () -> assertEquals("m-order-1", actual.getOrderId()),
                () -> assertEquals("PENDING", actual.getOrderStatus()),
                () -> assertEquals(RequestMethod.SBP, actual.getDetails().getRequestMethod()),
                () -> assertEquals("Сбербанк", actual.getDetails().getBank()),
                () -> assertEquals(2003, actual.getAmount())
        );
        verify(onlyPays, never()).cancelOrder(any());
    }

    @Test
    void getDetailsShouldKeepAmountNullIfMerchantDidNotChangeIt() {
        givenMerchant(onlyPays, Merchant.ONLY_PAYS, "CARD_ONLY_PAYS");
        when(onlyPays.createOrder(any())).thenReturn(Optional.of(merchantResponse(Merchant.ONLY_PAYS, "Т-Банк", null)));

        ApiDetailsResponse actual = service.getDetails(Merchant.ONLY_PAYS, request(null, RequestMethod.CARD)).orElseThrow();

        assertNull(actual.getAmount());
    }

    @Test
    void getDetailsShouldPassNullUserIdToMerchant() {
        givenMerchant(onlyPays, Merchant.ONLY_PAYS, "CARD_ONLY_PAYS");
        when(onlyPays.createOrder(any())).thenReturn(Optional.of(merchantResponse(Merchant.ONLY_PAYS, "Т-Банк", null)));

        service.getDetails(Merchant.ONLY_PAYS, request(null, RequestMethod.CARD));

        ArgumentCaptor<OrderCreationRequest> captor = ArgumentCaptor.forClass(OrderCreationRequest.class);
        verify(onlyPays).createOrder(captor.capture());
        assertAll(
                () -> assertNull(captor.getValue().getUserId()),
                () -> assertEquals("CARD_ONLY_PAYS", captor.getValue().getMethod()),
                () -> assertEquals(2000, captor.getValue().getAmount())
        );
    }

    @Test
    void getDetailsShouldPassDetailsWithoutBankToProcessing() {
        givenMerchant(onlyPays, Merchant.ONLY_PAYS, "SBP_ONLY_PAYS");
        when(onlyPays.createOrder(any())).thenReturn(Optional.of(merchantResponse(Merchant.ONLY_PAYS, null, null)));

        ApiDetailsResponse actual = service.getDetails(Merchant.ONLY_PAYS, request(null, RequestMethod.SBP)).orElseThrow();

        assertNull(actual.getDetails().getBank());
        verify(onlyPays, never()).cancelOrder(any());
    }

    @Test
    void getDetailsShouldTryNextMethodIfMerchantReturnedNothing() {
        when(merchantServiceRegistry.getService(Merchant.ONLY_PAYS)).thenReturn(Optional.of(onlyPays));
        when(requestMethodService.getMerchantMethods(Merchant.ONLY_PAYS, List.of(RequestMethod.CARD)))
                .thenReturn(List.of("CARD_ONLY_PAYS"));
        when(requestMethodService.getMerchantMethods(Merchant.ONLY_PAYS, List.of(RequestMethod.SBP)))
                .thenReturn(List.of("SBP_ONLY_PAYS"));
        when(onlyPays.createOrder(any()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(merchantResponse(Merchant.ONLY_PAYS, "Сбербанк", null)));
        ApiDetailsRequest request = request(null, RequestMethod.CARD, RequestMethod.SBP);

        ApiDetailsResponse actual = service.getDetails(Merchant.ONLY_PAYS, request).orElseThrow();

        assertProcessingContract(actual, request);
        assertEquals(RequestMethod.SBP, actual.getDetails().getRequestMethod());
    }

    @Test
    void getDetailsShouldTryNextMerchantIfMerchantReturnedNothing() {
        givenTurnedOn(Merchant.EVO_PAY, Merchant.ONLY_PAYS);
        givenMerchant(evoPay, Merchant.EVO_PAY, "CARD_EVO_PAY");
        givenMerchant(onlyPays, Merchant.ONLY_PAYS, "CARD_ONLY_PAYS");
        when(evoPay.createOrder(any())).thenReturn(Optional.empty());
        when(onlyPays.createOrder(any())).thenReturn(Optional.of(merchantResponse(Merchant.ONLY_PAYS, "Т-Банк", null)));
        ApiDetailsRequest request = request(null, RequestMethod.CARD);

        ApiDetailsResponse actual = service.getDetails(request).orElseThrow();

        assertProcessingContract(actual, request);
        assertEquals("ONLY_PAYS", actual.getMerchant());
    }

    @Test
    void getDetailsShouldReturnEmptyIfNoMerchantIsTurnedOn() {
        when(requestMethodService.getMerchants(anyList())).thenReturn(Set.of(Merchant.ONLY_PAYS));
        when(merchantConfigService.findAllTurnedByMerchantsAndOwnerId(any(), eq(OWNER_ID))).thenReturn(List.of());

        assertTrue(service.getDetails(request(null, RequestMethod.CARD)).isEmpty());
        verifyNoInteractions(merchantServiceRegistry);
    }
}
