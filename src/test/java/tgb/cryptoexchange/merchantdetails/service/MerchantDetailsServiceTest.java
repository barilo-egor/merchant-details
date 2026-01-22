package tgb.cryptoexchange.merchantdetails.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.constants.VariableType;
import tgb.cryptoexchange.merchantdetails.details.*;
import tgb.cryptoexchange.merchantdetails.details.bridgepay.Method;
import tgb.cryptoexchange.merchantdetails.entity.MerchantConfig;
import tgb.cryptoexchange.merchantdetails.entity.Variable;
import tgb.cryptoexchange.merchantdetails.kafka.MerchantDetailsReceiveEventProducer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MerchantDetailsServiceTest {

    @Mock
    private MerchantServiceRegistry merchantServiceRegistry;

    @Mock
    private MerchantDetailsReceiveEventProducer merchantDetailsReceiveEventProducer;

    @Mock
    private MerchantConfigService merchantConfigService;

    @Mock
    private VariableService variableService;

    @Mock
    private SleepService sleepService;

    @InjectMocks
    private MerchantDetailsService merchantDetailsService;

    @Test
    void getDetailsShouldReturnEmptyOptionalIfMerchantServiceNotImplemented() {
        when(merchantServiceRegistry.getService(any())).thenReturn(Optional.empty());
        assertTrue(merchantDetailsService.getDetails(Merchant.ALFA_TEAM, new DetailsRequest()).isEmpty());
    }

    @Test
    void getDetailsShouldReturnEmptyOptionalIfMerchantReturnNoDetails() {
        MerchantService merchantService = Mockito.mock(MerchantService.class);
        when(merchantServiceRegistry.getService(any())).thenReturn(Optional.of(merchantService));
        when(merchantService.createOrder(any())).thenReturn(Optional.empty());
        assertTrue(merchantDetailsService.getDetails(Merchant.ALFA_TEAM, new DetailsRequest()).isEmpty());
    }

    @Test
    void getDetailsShouldReturnDetailsAndPutDetailsToProducer() {
        MerchantService merchantService = Mockito.mock(MerchantService.class);
        when(merchantServiceRegistry.getService(any())).thenReturn(Optional.of(merchantService));
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setId(50005L);
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(Merchant.ALFA_TEAM);
        detailsResponse.setDetails("SOME BANK 1234 1234 1234 1234");
        when(merchantService.createOrder(any())).thenReturn(Optional.of(detailsResponse));
        ArgumentCaptor<Merchant> merchantCaptor = ArgumentCaptor.forClass(Merchant.class);
        ArgumentCaptor<DetailsRequest> detailsRequestCaptor = ArgumentCaptor.forClass(DetailsRequest.class);
        ArgumentCaptor<DetailsResponse> detailsResponseCaptor = ArgumentCaptor.forClass(DetailsResponse.class);
        Optional<DetailsResponse> maybeResponse = merchantDetailsService.getDetails(Merchant.ALFA_TEAM, detailsRequest);
        assertTrue(maybeResponse.isPresent());
        DetailsResponse actual = maybeResponse.get();
        assertEquals(Merchant.ALFA_TEAM, actual.getMerchant());
        verify(merchantDetailsReceiveEventProducer).put(merchantCaptor.capture(), detailsRequestCaptor.capture(), detailsResponseCaptor.capture());
        assertAll(
                () -> assertEquals(Merchant.ALFA_TEAM, merchantCaptor.getValue()),
                () -> assertEquals(detailsRequest, detailsRequestCaptor.getValue()),
                () -> assertEquals(detailsResponse, detailsResponseCaptor.getValue())
        );
    }

    @CsvSource(delimiter = ';', textBlock = """
            ALFA_TEAM;{"someField":"someValue","merchant":"ALFA_TEAM"}
            WELL_BIT;{"someField":"someValue","merchant":"WELL_BIT"}
            """)
    @ParameterizedTest
    void updateStatusShouldCallMerchantServiceUpdateStatusMethod(Merchant merchant, String body) {
        MerchantService merchantService = Mockito.mock(MerchantService.class);
        when(merchantServiceRegistry.getService(merchant)).thenReturn(Optional.of(merchantService));
        merchantDetailsService.updateStatus(merchant, body);
        verify(merchantService).updateStatus(body);
    }

    @CsvSource(textBlock = """
            ALFA_TEAM,20fb47cc-bbb2-4e39-84db-9f67c0b2900e,CARD
            WELL_BIT,4e995450-91f6-4b59-b952-7c02ce7b6fdc,SBP
            """)
    @ParameterizedTest
    void updateStatusShouldCallMerchantServiceUpdateStatusMethod(Merchant merchant, String orderId, String method) {
        MerchantService merchantService = Mockito.mock(MerchantService.class);
        when(merchantServiceRegistry.getService(merchant)).thenReturn(Optional.of(merchantService));
        CancelOrderRequest cancelOrderRequest = new CancelOrderRequest();
        cancelOrderRequest.setOrderId(orderId);
        cancelOrderRequest.setMethod(method);
        merchantDetailsService.cancelOrder(merchant, cancelOrderRequest);
        verify(merchantService).cancelOrder(cancelOrderRequest);
    }

    @Test
    void getDetailsShouldReturnEmptyOptionalIfNoMerchantConfigs() {
        when(merchantConfigService.findAllByMethodsAndAmount(any(), anyInt())).thenReturn(new ArrayList<>());
        when(variableService.findByType(VariableType.ATTEMPTS_COUNT))
                .thenReturn(Variable.builder().type(VariableType.ATTEMPTS_COUNT).value("3").build());
        when(variableService.findByType(VariableType.MIN_ATTEMPT_TIME))
                .thenReturn(Variable.builder().type(VariableType.MIN_ATTEMPT_TIME).value("15").build());
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setMethods(new ArrayList<>());
        detailsRequest.setAmount(1000);
        assertTrue(merchantDetailsService.getDetails(detailsRequest).isEmpty());
    }

    @Test
    void getDetailsShouldReturnEmptyOptionalIfOneMerchantHasNoDetails() {
        DetailsRequest detailsRequest = new DetailsRequest();
        List<DetailsRequest.MerchantMethod> merchantMethods = new ArrayList<>();
        merchantMethods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.ALFA_TEAM).method(Method.TO_CARD.name()).build());
        detailsRequest.setMethods(merchantMethods);
        detailsRequest.setAmount(1000);

        List<MerchantConfig> merchantConfigs = new ArrayList<>();
        merchantConfigs.add(MerchantConfig.builder()
                .merchant(Merchant.ALFA_TEAM)
                .build()
        );
        when(merchantConfigService.findAllByMethodsAndAmount(any(), anyInt())).thenReturn(merchantConfigs);
        when(variableService.findByType(VariableType.ATTEMPTS_COUNT))
                .thenReturn(Variable.builder().type(VariableType.ATTEMPTS_COUNT).value("3").build());
        when(variableService.findByType(VariableType.MIN_ATTEMPT_TIME))
                .thenReturn(Variable.builder().type(VariableType.MIN_ATTEMPT_TIME).value("15").build());

        MerchantService merchantService = Mockito.mock(MerchantService.class);
        when(merchantServiceRegistry.getService(Merchant.ALFA_TEAM)).thenReturn(Optional.of(merchantService));
        when(merchantService.createOrder(detailsRequest)).thenReturn(Optional.empty());

        assertTrue(merchantDetailsService.getDetails(detailsRequest).isEmpty());
    }

    @Test
    void getDetailsShouldReturnDetailsIfOneMerchantHasDetails() {
        DetailsRequest detailsRequest = new DetailsRequest();
        List<DetailsRequest.MerchantMethod> merchantMethods = new ArrayList<>();
        merchantMethods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.ALFA_TEAM).method(Method.TO_CARD.name()).build());
        detailsRequest.setMethods(merchantMethods);
        detailsRequest.setAmount(1000);

        List<MerchantConfig> merchantConfigs = new ArrayList<>();
        merchantConfigs.add(MerchantConfig.builder()
                .merchant(Merchant.ALFA_TEAM)
                .build()
        );
        when(merchantConfigService.findAllByMethodsAndAmount(any(), anyInt())).thenReturn(merchantConfigs);
        when(variableService.findByType(VariableType.ATTEMPTS_COUNT))
                .thenReturn(Variable.builder().type(VariableType.ATTEMPTS_COUNT).value("3").build());

        MerchantService merchantService = Mockito.mock(MerchantService.class);
        when(merchantServiceRegistry.getService(Merchant.ALFA_TEAM)).thenReturn(Optional.of(merchantService));
        DetailsResponse detailsResponse = new DetailsResponse();
        String expectedId = UUID.randomUUID().toString();
        detailsResponse.setMerchantOrderId(expectedId);
        detailsResponse.setMerchant(Merchant.ALFA_TEAM);
        when(merchantService.createOrder(detailsRequest)).thenReturn(Optional.of(detailsResponse));

        Optional<DetailsResponse> maybeDetailsResponse = merchantDetailsService.getDetails(detailsRequest);
        assertTrue(maybeDetailsResponse.isPresent());
        assertEquals(expectedId, maybeDetailsResponse.get().getMerchantOrderId());
        assertEquals(Merchant.ALFA_TEAM, maybeDetailsResponse.get().getMerchant());
    }

    @Test
    void getDetailsShouldReturnDetailsIfFirstHasDetails() {
        DetailsRequest detailsRequest = new DetailsRequest();
        List<DetailsRequest.MerchantMethod> merchantMethods = new ArrayList<>();
        merchantMethods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.ALFA_TEAM).method(Method.TO_CARD.name()).build());
        merchantMethods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.ONLY_PAYS).method(Method.TO_CARD.name()).build());
        merchantMethods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.EVO_PAY).method(Method.TO_CARD.name()).build());
        merchantMethods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.HONEY_MONEY).method(Method.TO_CARD.name()).build());
        detailsRequest.setMethods(merchantMethods);
        detailsRequest.setAmount(1000);

        List<MerchantConfig> merchantConfigs = new ArrayList<>();
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.ALFA_TEAM).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.ONLY_PAYS).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.EVO_PAY).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.HONEY_MONEY).build());
        when(merchantConfigService.findAllByMethodsAndAmount(any(), anyInt())).thenReturn(merchantConfigs);
        when(variableService.findByType(VariableType.ATTEMPTS_COUNT))
                .thenReturn(Variable.builder().type(VariableType.ATTEMPTS_COUNT).value("3").build());

        MerchantService merchantService = Mockito.mock(MerchantService.class);
        when(merchantServiceRegistry.getService(Merchant.ALFA_TEAM)).thenReturn(Optional.of(merchantService));
        DetailsResponse detailsResponse = new DetailsResponse();
        String expectedId = UUID.randomUUID().toString();
        detailsResponse.setMerchantOrderId(expectedId);
        detailsResponse.setMerchant(Merchant.ALFA_TEAM);
        when(merchantService.createOrder(detailsRequest)).thenReturn(Optional.of(detailsResponse));

        Optional<DetailsResponse> maybeDetailsResponse = merchantDetailsService.getDetails(detailsRequest);
        assertTrue(maybeDetailsResponse.isPresent());
        assertEquals(expectedId, maybeDetailsResponse.get().getMerchantOrderId());
    }

    @Test
    void getDetailsShouldReturnDetailsIfLastHasDetails() {
        DetailsRequest detailsRequest = new DetailsRequest();
        List<DetailsRequest.MerchantMethod> merchantMethods = new ArrayList<>();
        merchantMethods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.ALFA_TEAM).method(Method.TO_CARD.name()).build());
        merchantMethods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.ONLY_PAYS).method(Method.TO_CARD.name()).build());
        merchantMethods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.EVO_PAY).method(Method.TO_CARD.name()).build());
        merchantMethods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.HONEY_MONEY).method(Method.TO_CARD.name()).build());
        detailsRequest.setMethods(merchantMethods);
        detailsRequest.setAmount(1000);

        List<MerchantConfig> merchantConfigs = new ArrayList<>();
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.ALFA_TEAM).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.ONLY_PAYS).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.EVO_PAY).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.HONEY_MONEY).build());
        when(merchantConfigService.findAllByMethodsAndAmount(any(), anyInt())).thenReturn(merchantConfigs);
        when(variableService.findByType(VariableType.ATTEMPTS_COUNT))
                .thenReturn(Variable.builder().type(VariableType.ATTEMPTS_COUNT).value("3").build());

        MerchantService alfaMerchantService = Mockito.mock(MerchantService.class);
        when(merchantServiceRegistry.getService(Merchant.ALFA_TEAM)).thenReturn(Optional.of(alfaMerchantService));

        MerchantService onlyPaysMerchantService = Mockito.mock(MerchantService.class);
        when(onlyPaysMerchantService.createOrder(detailsRequest)).thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.ONLY_PAYS)).thenReturn(Optional.of(onlyPaysMerchantService));

        MerchantService evoPayMerchantService = Mockito.mock(MerchantService.class);
        when(evoPayMerchantService.createOrder(detailsRequest)).thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.EVO_PAY)).thenReturn(Optional.of(evoPayMerchantService));

        DetailsResponse detailsResponse = new DetailsResponse();
        String expectedId = UUID.randomUUID().toString();
        detailsResponse.setMerchantOrderId(expectedId);
        detailsResponse.setMerchant(Merchant.HONEY_MONEY);

        MerchantService honeyMoneyMerchantService = Mockito.mock(MerchantService.class);
        when(honeyMoneyMerchantService.createOrder(detailsRequest)).thenReturn(Optional.of(detailsResponse));
        when(merchantServiceRegistry.getService(Merchant.HONEY_MONEY)).thenReturn(Optional.of(honeyMoneyMerchantService));

        Optional<DetailsResponse> maybeDetailsResponse = merchantDetailsService.getDetails(detailsRequest);
        assertTrue(maybeDetailsResponse.isPresent());
        assertEquals(expectedId, maybeDetailsResponse.get().getMerchantOrderId());
        assertEquals(Merchant.HONEY_MONEY, maybeDetailsResponse.get().getMerchant());
    }

    @Test
    void getDetailsShouldReturnDetailsIfNoOneHasDetails() {
        DetailsRequest detailsRequest = new DetailsRequest();
        List<DetailsRequest.MerchantMethod> merchantMethods = new ArrayList<>();
        merchantMethods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.ALFA_TEAM).method(Method.TO_CARD.name()).build());
        merchantMethods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.ONLY_PAYS).method(Method.TO_CARD.name()).build());
        merchantMethods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.EVO_PAY).method(Method.TO_CARD.name()).build());
        merchantMethods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.HONEY_MONEY).method(Method.TO_CARD.name()).build());
        detailsRequest.setMethods(merchantMethods);
        detailsRequest.setAmount(1000);

        List<MerchantConfig> merchantConfigs = new ArrayList<>();
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.ALFA_TEAM).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.ONLY_PAYS).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.EVO_PAY).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.HONEY_MONEY).build());
        when(merchantConfigService.findAllByMethodsAndAmount(any(), anyInt())).thenReturn(merchantConfigs);
        when(variableService.findByType(VariableType.ATTEMPTS_COUNT))
                .thenReturn(Variable.builder().type(VariableType.ATTEMPTS_COUNT).value("3").build());
        when(variableService.findByType(VariableType.MIN_ATTEMPT_TIME))
                .thenReturn(Variable.builder().type(VariableType.MIN_ATTEMPT_TIME).value("15").build());

        MerchantService alfaMerchantService = Mockito.mock(MerchantService.class);
        when(alfaMerchantService.createOrder(detailsRequest)).thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.ALFA_TEAM)).thenReturn(Optional.of(alfaMerchantService));

        MerchantService onlyPaysMerchantService = Mockito.mock(MerchantService.class);
        when(onlyPaysMerchantService.createOrder(detailsRequest)).thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.ONLY_PAYS)).thenReturn(Optional.of(onlyPaysMerchantService));

        MerchantService evoPayMerchantService = Mockito.mock(MerchantService.class);
        when(evoPayMerchantService.createOrder(detailsRequest)).thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.EVO_PAY)).thenReturn(Optional.of(evoPayMerchantService));

        MerchantService honeyMoneyMerchantService = Mockito.mock(MerchantService.class);
        when(honeyMoneyMerchantService.createOrder(detailsRequest)).thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.HONEY_MONEY)).thenReturn(Optional.of(honeyMoneyMerchantService));

        Optional<DetailsResponse> maybeDetailsResponse = merchantDetailsService.getDetails(detailsRequest);
        assertTrue(maybeDetailsResponse.isEmpty());
    }

    @Test
    void getDetailsShouldReturnDetailsIfFirstHasDetailsOnSecondIteration() {
        DetailsRequest detailsRequest = new DetailsRequest();
        List<DetailsRequest.MerchantMethod> merchantMethods = new ArrayList<>();
        merchantMethods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.ALFA_TEAM).method(Method.TO_CARD.name()).build());
        merchantMethods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.ONLY_PAYS).method(Method.TO_CARD.name()).build());
        merchantMethods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.EVO_PAY).method(Method.TO_CARD.name()).build());
        merchantMethods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.HONEY_MONEY).method(Method.TO_CARD.name()).build());
        detailsRequest.setMethods(merchantMethods);
        detailsRequest.setAmount(1000);

        List<MerchantConfig> merchantConfigs = new ArrayList<>();
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.ALFA_TEAM).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.ONLY_PAYS).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.EVO_PAY).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.HONEY_MONEY).build());
        when(merchantConfigService.findAllByMethodsAndAmount(any(), anyInt())).thenReturn(merchantConfigs);
        when(variableService.findByType(VariableType.ATTEMPTS_COUNT))
                .thenReturn(Variable.builder().type(VariableType.ATTEMPTS_COUNT).value("3").build());
        when(variableService.findByType(VariableType.MIN_ATTEMPT_TIME))
                .thenReturn(Variable.builder().type(VariableType.MIN_ATTEMPT_TIME).value("15").build());

        DetailsResponse detailsResponse = new DetailsResponse();
        String expectedId = UUID.randomUUID().toString();
        detailsResponse.setMerchantOrderId(expectedId);
        detailsResponse.setMerchant(Merchant.ALFA_TEAM);

        MerchantService alfaMerchantService = Mockito.mock(MerchantService.class);
        when(alfaMerchantService.createOrder(detailsRequest)).thenReturn(Optional.empty()).thenReturn(Optional.of(detailsResponse));
        when(merchantServiceRegistry.getService(Merchant.ALFA_TEAM)).thenReturn(Optional.of(alfaMerchantService));

        MerchantService onlyPaysMerchantService = Mockito.mock(MerchantService.class);
        when(onlyPaysMerchantService.createOrder(detailsRequest)).thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.ONLY_PAYS)).thenReturn(Optional.of(onlyPaysMerchantService));

        MerchantService evoPayMerchantService = Mockito.mock(MerchantService.class);
        when(evoPayMerchantService.createOrder(detailsRequest)).thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.EVO_PAY)).thenReturn(Optional.of(evoPayMerchantService));


        MerchantService honeyMoneyMerchantService = Mockito.mock(MerchantService.class);
        when(honeyMoneyMerchantService.createOrder(detailsRequest)).thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.HONEY_MONEY)).thenReturn(Optional.of(honeyMoneyMerchantService));

        Optional<DetailsResponse> maybeDetailsResponse = merchantDetailsService.getDetails(detailsRequest);
        assertTrue(maybeDetailsResponse.isPresent());
        assertEquals(expectedId, maybeDetailsResponse.get().getMerchantOrderId());
        assertEquals(Merchant.ALFA_TEAM, maybeDetailsResponse.get().getMerchant());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    void getDetailsShouldTryAttemptsCountTimes(Integer times) {
        DetailsRequest detailsRequest = new DetailsRequest();
        List<DetailsRequest.MerchantMethod> merchantMethods = new ArrayList<>();
        merchantMethods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.ALFA_TEAM).method(Method.TO_CARD.name()).build());
        merchantMethods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.ONLY_PAYS).method(Method.TO_CARD.name()).build());
        merchantMethods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.EVO_PAY).method(Method.TO_CARD.name()).build());
        merchantMethods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.HONEY_MONEY).method(Method.TO_CARD.name()).build());
        detailsRequest.setMethods(merchantMethods);
        detailsRequest.setAmount(1000);

        List<MerchantConfig> merchantConfigs = new ArrayList<>();
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.ALFA_TEAM).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.ONLY_PAYS).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.EVO_PAY).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.HONEY_MONEY).build());
        when(merchantConfigService.findAllByMethodsAndAmount(any(), anyInt())).thenReturn(merchantConfigs);
        when(variableService.findByType(VariableType.ATTEMPTS_COUNT))
                .thenReturn(Variable.builder().type(VariableType.ATTEMPTS_COUNT).value(String.valueOf(times)).build());
        if (times > 1) {
            when(variableService.findByType(VariableType.MIN_ATTEMPT_TIME))
                    .thenReturn(Variable.builder().type(VariableType.MIN_ATTEMPT_TIME).value("15").build());
        }

        MerchantService alfaMerchantService = Mockito.mock(MerchantService.class);
        when(alfaMerchantService.createOrder(detailsRequest)).thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.ALFA_TEAM)).thenReturn(Optional.of(alfaMerchantService));

        MerchantService onlyPaysMerchantService = Mockito.mock(MerchantService.class);
        when(onlyPaysMerchantService.createOrder(detailsRequest)).thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.ONLY_PAYS)).thenReturn(Optional.of(onlyPaysMerchantService));

        MerchantService evoPayMerchantService = Mockito.mock(MerchantService.class);
        when(evoPayMerchantService.createOrder(detailsRequest)).thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.EVO_PAY)).thenReturn(Optional.of(evoPayMerchantService));


        MerchantService honeyMoneyMerchantService = Mockito.mock(MerchantService.class);
        when(honeyMoneyMerchantService.createOrder(detailsRequest)).thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.HONEY_MONEY)).thenReturn(Optional.of(honeyMoneyMerchantService));

        merchantDetailsService.getDetails(detailsRequest);
        verify(alfaMerchantService, times(times)).createOrder(detailsRequest);
        verify(onlyPaysMerchantService, times(times)).createOrder(detailsRequest);
        verify(evoPayMerchantService, times(times)).createOrder(detailsRequest);
        verify(honeyMoneyMerchantService, times(times)).createOrder(detailsRequest);
    }

    @Test
    void getDetailsShouldStopTryIfDetailsFounded() {
        DetailsRequest detailsRequest = new DetailsRequest();
        List<DetailsRequest.MerchantMethod> merchantMethods = new ArrayList<>();
        merchantMethods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.ALFA_TEAM).method(Method.TO_CARD.name()).build());
        merchantMethods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.ONLY_PAYS).method(Method.TO_CARD.name()).build());
        merchantMethods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.EVO_PAY).method(Method.TO_CARD.name()).build());
        merchantMethods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.HONEY_MONEY).method(Method.TO_CARD.name()).build());
        detailsRequest.setMethods(merchantMethods);
        detailsRequest.setAmount(1000);

        List<MerchantConfig> merchantConfigs = new ArrayList<>();
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.ALFA_TEAM).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.ONLY_PAYS).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.EVO_PAY).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.HONEY_MONEY).build());
        when(merchantConfigService.findAllByMethodsAndAmount(any(), anyInt())).thenReturn(merchantConfigs);
        when(variableService.findByType(VariableType.ATTEMPTS_COUNT))
                .thenReturn(Variable.builder().type(VariableType.ATTEMPTS_COUNT).value("3").build());
        when(variableService.findByType(VariableType.MIN_ATTEMPT_TIME))
                .thenReturn(Variable.builder().type(VariableType.MIN_ATTEMPT_TIME).value("15").build());

        DetailsResponse detailsResponse = new DetailsResponse();
        String expectedId = UUID.randomUUID().toString();
        detailsResponse.setMerchantOrderId(expectedId);
        detailsResponse.setMerchant(Merchant.HONEY_MONEY);

        MerchantService alfaMerchantService = Mockito.mock(MerchantService.class);
        when(alfaMerchantService.createOrder(detailsRequest))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(detailsResponse));
        when(merchantServiceRegistry.getService(Merchant.ALFA_TEAM)).thenReturn(Optional.of(alfaMerchantService));

        MerchantService onlyPaysMerchantService = Mockito.mock(MerchantService.class);
        when(onlyPaysMerchantService.createOrder(detailsRequest)).thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.ONLY_PAYS)).thenReturn(Optional.of(onlyPaysMerchantService));

        MerchantService evoPayMerchantService = Mockito.mock(MerchantService.class);
        when(evoPayMerchantService.createOrder(detailsRequest)).thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.EVO_PAY)).thenReturn(Optional.of(evoPayMerchantService));


        MerchantService honeyMoneyMerchantService = Mockito.mock(MerchantService.class);
        when(honeyMoneyMerchantService.createOrder(detailsRequest)).thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.HONEY_MONEY)).thenReturn(Optional.of(honeyMoneyMerchantService));

        merchantDetailsService.getDetails(detailsRequest);
        verify(alfaMerchantService, times(3)).createOrder(detailsRequest);
        verify(onlyPaysMerchantService, times(2)).createOrder(detailsRequest);
        verify(evoPayMerchantService, times(2)).createOrder(detailsRequest);
        verify(honeyMoneyMerchantService, times(2)).createOrder(detailsRequest);
    }

    @Test
    void getDetailsShouldKeepTryAfterExceptions() {
        DetailsRequest detailsRequest = new DetailsRequest();
        List<DetailsRequest.MerchantMethod> merchantMethods = new ArrayList<>();
        merchantMethods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.ALFA_TEAM).method(Method.TO_CARD.name()).build());
        merchantMethods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.ONLY_PAYS).method(Method.TO_CARD.name()).build());
        merchantMethods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.EVO_PAY).method(Method.TO_CARD.name()).build());
        merchantMethods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.HONEY_MONEY).method(Method.TO_CARD.name()).build());
        detailsRequest.setMethods(merchantMethods);
        detailsRequest.setAmount(1000);

        List<MerchantConfig> merchantConfigs = new ArrayList<>();
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.ALFA_TEAM).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.ONLY_PAYS).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.EVO_PAY).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.HONEY_MONEY).build());
        when(merchantConfigService.findAllByMethodsAndAmount(any(), anyInt())).thenReturn(merchantConfigs);
        when(variableService.findByType(VariableType.ATTEMPTS_COUNT))
                .thenReturn(Variable.builder().type(VariableType.ATTEMPTS_COUNT).value("3").build());
        when(variableService.findByType(VariableType.MIN_ATTEMPT_TIME))
                .thenReturn(Variable.builder().type(VariableType.MIN_ATTEMPT_TIME).value("15").build());

        DetailsResponse detailsResponse = new DetailsResponse();
        String expectedId = UUID.randomUUID().toString();
        detailsResponse.setMerchantOrderId(expectedId);
        detailsResponse.setMerchant(Merchant.HONEY_MONEY);

        MerchantService alfaMerchantService = Mockito.mock(MerchantService.class);
        when(alfaMerchantService.createOrder(detailsRequest))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(detailsResponse));
        when(merchantServiceRegistry.getService(Merchant.ALFA_TEAM)).thenReturn(Optional.of(alfaMerchantService));

        MerchantService onlyPaysMerchantService = Mockito.mock(MerchantService.class);
        when(onlyPaysMerchantService.createOrder(detailsRequest))
                .thenThrow(RuntimeException.class)
                .thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.ONLY_PAYS)).thenReturn(Optional.of(onlyPaysMerchantService));

        MerchantService evoPayMerchantService = Mockito.mock(MerchantService.class);
        when(evoPayMerchantService.createOrder(detailsRequest))
                .thenThrow(RuntimeException.class)
                .thenThrow(RuntimeException.class)
                .thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.EVO_PAY)).thenReturn(Optional.of(evoPayMerchantService));


        MerchantService honeyMoneyMerchantService = Mockito.mock(MerchantService.class);
        when(honeyMoneyMerchantService.createOrder(detailsRequest)).thenThrow(RuntimeException.class);
        when(merchantServiceRegistry.getService(Merchant.HONEY_MONEY)).thenReturn(Optional.of(honeyMoneyMerchantService));

        merchantDetailsService.getDetails(detailsRequest);
        verify(alfaMerchantService, times(3)).createOrder(detailsRequest);
        verify(onlyPaysMerchantService, times(2)).createOrder(detailsRequest);
        verify(evoPayMerchantService, times(2)).createOrder(detailsRequest);
        verify(honeyMoneyMerchantService, times(2)).createOrder(detailsRequest);
    }


    @CsvSource("""
            ALFA_TEAM,c2b38482-3901-44e4-a2ee-ef706bdea3e6
            BIT_ZONE,54907906-cb06-471f-8a10-7506803f3354
            """)
    @ParameterizedTest
    void sendReceiptShouldCallMerchantServiceMethod(Merchant merchant, String orderId) {
        MerchantService merchantService = mock(MerchantService.class);
        MultipartFile file = mock(MultipartFile.class);
        when(merchantServiceRegistry.getService(merchant)).thenReturn(Optional.of(merchantService));
        merchantDetailsService.sendReceipt(merchant, orderId, file);
        verify(merchantService).sendReceipt(orderId, file);
    }
}