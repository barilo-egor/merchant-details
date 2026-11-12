package net.rcetech.md.app.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import net.rcetech.md.meta.enums.RequiredReceipt;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.meta.constants.VariableType;
import net.rcetech.md.app.details.*;
import net.rcetech.md.integrations.platform.*;
import net.rcetech.md.integrations.platform.bridgepay.Method;
import net.rcetech.md.domain.entity.MerchantConfig;
import net.rcetech.md.domain.entity.Variable;
import net.rcetech.md.meta.enums.ConfigType;
import net.rcetech.md.app.kafka.MerchantDetailsReceiveEventProducer;

import java.util.*;

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

    private final MeterRegistry meterRegistry = new SimpleMeterRegistry();

    private MerchantDetailsService merchantDetailsService;

    @BeforeEach
    void setUp() {
        merchantDetailsService = new MerchantDetailsService(merchantServiceRegistry, merchantDetailsReceiveEventProducer,
                merchantConfigService, variableService, sleepService, meterRegistry);
    }

    @Test
    void getDetailsShouldReturnEmptyOptionalIfMerchantServiceNotImplemented() {
        when(merchantServiceRegistry.getService(any())).thenReturn(Optional.empty());
        assertTrue(merchantDetailsService.getDetails(Merchant.ALFA_TEAM, null, new BotDetailsRequest()).isEmpty());
    }

    @Test
    void getDetailsShouldReturnEmptyOptionalIfMerchantReturnNoDetails() {
        BotDetailsRequest detailsRequest = new BotDetailsRequest();
        detailsRequest.setMethods(List.of(BotDetailsRequest.MerchantMethod.builder().merchant(Merchant.ALFA_TEAM).methods(Collections.singletonList("TO_CARD")).build()));
        MerchantService merchantService = Mockito.mock(MerchantService.class);
        when(merchantServiceRegistry.getService(any())).thenReturn(Optional.of(merchantService));
        when(merchantService.createOrder(any())).thenReturn(Optional.empty());
        assertTrue(merchantDetailsService.getDetails(Merchant.ALFA_TEAM, null, detailsRequest).isEmpty());
    }

    @Test
    void getDetailsShouldReturnDetailsAndPutDetailsToProducer() {
        MerchantService merchantService = Mockito.mock(MerchantService.class);
        when(merchantServiceRegistry.getService(any())).thenReturn(Optional.of(merchantService));
        BotDetailsRequest detailsRequest = new BotDetailsRequest();
        detailsRequest.setId(String.valueOf(50005L));
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(Merchant.ALFA_TEAM);
        detailsResponse.setDetails("SOME BANK 1234 1234 1234 1234");
        detailsRequest.setMethods(List.of(BotDetailsRequest.MerchantMethod.builder().merchant(Merchant.ALFA_TEAM).methods(Collections.singletonList("TO_CARD")).build()));
        when(merchantService.createOrder(any())).thenReturn(Optional.of(detailsResponse));
        ArgumentCaptor<Merchant> merchantCaptor = ArgumentCaptor.forClass(Merchant.class);
        ArgumentCaptor<BotDetailsRequest> detailsRequestCaptor = ArgumentCaptor.forClass(BotDetailsRequest.class);
        ArgumentCaptor<DetailsResponse> detailsResponseCaptor = ArgumentCaptor.forClass(DetailsResponse.class);
        Optional<DetailsResponse> maybeResponse = merchantDetailsService.getDetails(Merchant.ALFA_TEAM, null, detailsRequest);
        assertTrue(maybeResponse.isPresent());
        DetailsResponse actual = maybeResponse.get();
        assertEquals(Merchant.ALFA_TEAM, actual.getMerchant());
        verify(merchantDetailsReceiveEventProducer).put(merchantCaptor.capture(), eq("TO_CARD"), detailsRequestCaptor.capture(), detailsResponseCaptor.capture());
        assertAll(
                () -> assertEquals(Merchant.ALFA_TEAM, merchantCaptor.getValue()),
                () -> assertEquals(detailsRequest, detailsRequestCaptor.getValue()),
                () -> assertEquals(detailsResponse, detailsResponseCaptor.getValue())
        );
    }

    @Test
    void getDetailsShouldReturnEmptyOptionalIfNoMerchantConfigs() {
        when(merchantConfigService.findAllByMethodsAndAmount(any(), anyInt())).thenReturn(new ArrayList<>());
        when(variableService.findByTypeAndConfigType(VariableType.ATTEMPTS_COUNT, ConfigType.BOT))
                .thenReturn(Variable.builder().type(VariableType.ATTEMPTS_COUNT).value("3").build());
        BotDetailsRequest detailsRequest = new BotDetailsRequest();
        detailsRequest.setMethods(new ArrayList<>());
        detailsRequest.setAmount(1000);
        detailsRequest.setInitiatorApp("bot");
        assertTrue(merchantDetailsService.getDetails(detailsRequest).isEmpty());
    }

    @Test
    void getDetailsShouldReturnEmptyOptionalIfOneMerchantHasNoDetails() {
        BotDetailsRequest detailsRequest = new BotDetailsRequest();
        List<BotDetailsRequest.MerchantMethod> merchantMethods = new ArrayList<>();
        merchantMethods.add(BotDetailsRequest.MerchantMethod.builder().merchant(Merchant.ALFA_TEAM).methods(Collections.singletonList(Method.TO_CARD.name())).build());
        detailsRequest.setMethods(merchantMethods);
        detailsRequest.setAmount(1000);
        detailsRequest.setInitiatorApp("bot");

        OrderCreationRequest creationRequest = new OrderCreationRequest();
        creationRequest.setMethod("TO_CARD");
        creationRequest.setAmount(1000);

        List<MerchantConfig> merchantConfigs = new ArrayList<>();
        merchantConfigs.add(MerchantConfig.builder()
                .merchant(Merchant.ALFA_TEAM)
                .build()
        );
        when(merchantConfigService.findAllByMethodsAndAmount(any(), anyInt())).thenReturn(merchantConfigs);
        when(variableService.findByTypeAndConfigType(VariableType.ATTEMPTS_COUNT, ConfigType.BOT))
                .thenReturn(Variable.builder().type(VariableType.ATTEMPTS_COUNT).value("3").build());
        when(variableService.findByTypeAndConfigType(VariableType.MIN_ATTEMPT_TIME, ConfigType.BOT))
                .thenReturn(Variable.builder().type(VariableType.MIN_ATTEMPT_TIME).value("15").build());

        MerchantService merchantService = Mockito.mock(MerchantService.class);
        when(merchantServiceRegistry.getService(Merchant.ALFA_TEAM)).thenReturn(Optional.of(merchantService));
        when(merchantService.createOrder(creationRequest)).thenReturn(Optional.empty());

        assertTrue(merchantDetailsService.getDetails(detailsRequest).isEmpty());
    }

    @Test
    void getDetailsShouldReturnDetailsIfOneMerchantHasDetails() {
        BotDetailsRequest detailsRequest = new BotDetailsRequest();
        List<BotDetailsRequest.MerchantMethod> merchantMethods = new ArrayList<>();
        merchantMethods.add(BotDetailsRequest.MerchantMethod.builder().merchant(Merchant.ALFA_TEAM).methods(Collections.singletonList(Method.TO_CARD.name())).build());
        detailsRequest.setMethods(merchantMethods);
        detailsRequest.setAmount(1000);
        detailsRequest.setInitiatorApp("bot");

        OrderCreationRequest creationRequest = new OrderCreationRequest();
        creationRequest.setMethod("TO_CARD");
        creationRequest.setAmount(1000);

        List<MerchantConfig> merchantConfigs = new ArrayList<>();
        merchantConfigs.add(MerchantConfig.builder()
                .merchant(Merchant.ALFA_TEAM)
                .build()
        );
        when(merchantConfigService.findAllByMethodsAndAmount(any(), anyInt())).thenReturn(merchantConfigs);
        when(variableService.findByTypeAndConfigType(VariableType.ATTEMPTS_COUNT, ConfigType.BOT))
                .thenReturn(Variable.builder().type(VariableType.ATTEMPTS_COUNT).value("3").build());

        MerchantService merchantService = Mockito.mock(MerchantService.class);
        when(merchantServiceRegistry.getService(Merchant.ALFA_TEAM)).thenReturn(Optional.of(merchantService));
        DetailsResponse detailsResponse = new DetailsResponse();
        String expectedId = UUID.randomUUID().toString();
        detailsResponse.setMerchantOrderId(expectedId);
        detailsResponse.setMerchant(Merchant.ALFA_TEAM);
        when(merchantService.createOrder(creationRequest)).thenReturn(Optional.of(detailsResponse));

        Optional<DetailsResponse> maybeDetailsResponse = merchantDetailsService.getDetails(detailsRequest);
        assertTrue(maybeDetailsResponse.isPresent());
        assertEquals(expectedId, maybeDetailsResponse.get().getMerchantOrderId());
        assertEquals(Merchant.ALFA_TEAM, maybeDetailsResponse.get().getMerchant());
    }

    @Test
    void getDetailsShouldPassRequiredReceiptFromMerchantConfigToOrderRequest() {
        BotDetailsRequest detailsRequest = new BotDetailsRequest();
        detailsRequest.setMethods(List.of(BotDetailsRequest.MerchantMethod.builder().merchant(Merchant.ALFA_TEAM)
                .methods(List.of(Method.TO_CARD.name())).build()));
        detailsRequest.setAmount(1000);
        detailsRequest.setInitiatorApp("bot");
        when(merchantConfigService.findAllByMethodsAndAmount(any(), anyInt())).thenReturn(List.of(MerchantConfig.builder()
                .merchant(Merchant.ALFA_TEAM)
                .requiredReceipt(RequiredReceipt.ANY)
                .build()));
        when(variableService.findByTypeAndConfigType(VariableType.ATTEMPTS_COUNT, ConfigType.BOT))
                .thenReturn(Variable.builder().type(VariableType.ATTEMPTS_COUNT).value("1").build());
        MerchantService merchantService = Mockito.mock(MerchantService.class);
        when(merchantServiceRegistry.getService(Merchant.ALFA_TEAM)).thenReturn(Optional.of(merchantService));
        when(merchantService.createOrder(any())).thenReturn(Optional.empty());

        merchantDetailsService.getDetails(detailsRequest);

        ArgumentCaptor<OrderCreationRequest> captor = ArgumentCaptor.forClass(OrderCreationRequest.class);
        verify(merchantService).createOrder(captor.capture());
        assertEquals(RequiredReceipt.ANY, captor.getValue().getRequiredReceipt());
    }

    @Test
    void getDetailsShouldReturnDetailsIfFirstHasDetails() {
        BotDetailsRequest detailsRequest = new BotDetailsRequest();
        List<BotDetailsRequest.MerchantMethod> merchantMethods = new ArrayList<>();
        merchantMethods.add(BotDetailsRequest.MerchantMethod.builder().merchant(Merchant.ALFA_TEAM).methods(Collections.singletonList(Method.TO_CARD.name())).build());
        merchantMethods.add(BotDetailsRequest.MerchantMethod.builder().merchant(Merchant.ONLY_PAYS).methods(Collections.singletonList(Method.TO_CARD.name())).build());
        merchantMethods.add(BotDetailsRequest.MerchantMethod.builder().merchant(Merchant.EVO_PAY).methods(Collections.singletonList(Method.TO_CARD.name())).build());
        merchantMethods.add(BotDetailsRequest.MerchantMethod.builder().merchant(Merchant.HONEY_MONEY).methods(Collections.singletonList(Method.TO_CARD.name())).build());
        detailsRequest.setMethods(merchantMethods);
        detailsRequest.setAmount(1000);
        detailsRequest.setInitiatorApp("bot");

        OrderCreationRequest creationRequest = new OrderCreationRequest();
        creationRequest.setMethod("TO_CARD");
        creationRequest.setAmount(1000);

        List<MerchantConfig> merchantConfigs = new ArrayList<>();
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.ALFA_TEAM).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.ONLY_PAYS).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.EVO_PAY).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.HONEY_MONEY).build());
        when(merchantConfigService.findAllByMethodsAndAmount(any(), anyInt())).thenReturn(merchantConfigs);
        when(variableService.findByTypeAndConfigType(VariableType.ATTEMPTS_COUNT, ConfigType.BOT))
                .thenReturn(Variable.builder().type(VariableType.ATTEMPTS_COUNT).value("3").build());

        MerchantService merchantService = Mockito.mock(MerchantService.class);
        when(merchantServiceRegistry.getService(Merchant.ALFA_TEAM)).thenReturn(Optional.of(merchantService));
        DetailsResponse detailsResponse = new DetailsResponse();
        String expectedId = UUID.randomUUID().toString();
        detailsResponse.setMerchantOrderId(expectedId);
        detailsResponse.setMerchant(Merchant.ALFA_TEAM);
        when(merchantService.createOrder(creationRequest)).thenReturn(Optional.of(detailsResponse));

        Optional<DetailsResponse> maybeDetailsResponse = merchantDetailsService.getDetails(detailsRequest);
        assertTrue(maybeDetailsResponse.isPresent());
        assertEquals(expectedId, maybeDetailsResponse.get().getMerchantOrderId());
    }

    @Test
    void getDetailsShouldReturnDetailsIfLastHasDetails() {
        BotDetailsRequest detailsRequest = new BotDetailsRequest();
        List<BotDetailsRequest.MerchantMethod> merchantMethods = new ArrayList<>();
        merchantMethods.add(BotDetailsRequest.MerchantMethod.builder().merchant(Merchant.ALFA_TEAM).methods(Collections.singletonList(Method.TO_CARD.name())).build());
        merchantMethods.add(BotDetailsRequest.MerchantMethod.builder().merchant(Merchant.ONLY_PAYS).methods(Collections.singletonList(Method.TO_CARD.name())).build());
        merchantMethods.add(BotDetailsRequest.MerchantMethod.builder().merchant(Merchant.EVO_PAY).methods(Collections.singletonList(Method.TO_CARD.name())).build());
        merchantMethods.add(BotDetailsRequest.MerchantMethod.builder().merchant(Merchant.HONEY_MONEY).methods(Collections.singletonList(Method.TO_CARD.name())).build());
        detailsRequest.setMethods(merchantMethods);
        detailsRequest.setAmount(1000);
        detailsRequest.setInitiatorApp("bot");

        OrderCreationRequest creationRequest = new OrderCreationRequest();
        creationRequest.setMethod("TO_CARD");
        creationRequest.setAmount(1000);

        List<MerchantConfig> merchantConfigs = new ArrayList<>();
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.ALFA_TEAM).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.ONLY_PAYS).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.EVO_PAY).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.HONEY_MONEY).build());
        when(merchantConfigService.findAllByMethodsAndAmount(any(), anyInt())).thenReturn(merchantConfigs);
        when(variableService.findByTypeAndConfigType(VariableType.ATTEMPTS_COUNT, ConfigType.BOT))
                .thenReturn(Variable.builder().type(VariableType.ATTEMPTS_COUNT).value("3").build());

        MerchantService alfaMerchantService = Mockito.mock(MerchantService.class);
        when(merchantServiceRegistry.getService(Merchant.ALFA_TEAM)).thenReturn(Optional.of(alfaMerchantService));

        MerchantService onlyPaysMerchantService = Mockito.mock(MerchantService.class);
        when(onlyPaysMerchantService.createOrder(creationRequest)).thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.ONLY_PAYS)).thenReturn(Optional.of(onlyPaysMerchantService));

        MerchantService evoPayMerchantService = Mockito.mock(MerchantService.class);
        when(evoPayMerchantService.createOrder(creationRequest)).thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.EVO_PAY)).thenReturn(Optional.of(evoPayMerchantService));

        DetailsResponse detailsResponse = new DetailsResponse();
        String expectedId = UUID.randomUUID().toString();
        detailsResponse.setMerchantOrderId(expectedId);
        detailsResponse.setMerchant(Merchant.HONEY_MONEY);

        MerchantService honeyMoneyMerchantService = Mockito.mock(MerchantService.class);
        when(honeyMoneyMerchantService.createOrder(creationRequest)).thenReturn(Optional.of(detailsResponse));
        when(merchantServiceRegistry.getService(Merchant.HONEY_MONEY)).thenReturn(Optional.of(honeyMoneyMerchantService));

        Optional<DetailsResponse> maybeDetailsResponse = merchantDetailsService.getDetails(detailsRequest);
        assertTrue(maybeDetailsResponse.isPresent());
        assertEquals(expectedId, maybeDetailsResponse.get().getMerchantOrderId());
        assertEquals(Merchant.HONEY_MONEY, maybeDetailsResponse.get().getMerchant());
    }

    @Test
    void getDetailsShouldReturnDetailsIfNoOneHasDetails() {
        BotDetailsRequest detailsRequest = new BotDetailsRequest();
        List<BotDetailsRequest.MerchantMethod> merchantMethods = new ArrayList<>();
        merchantMethods.add(BotDetailsRequest.MerchantMethod.builder().merchant(Merchant.ALFA_TEAM).methods(Collections.singletonList(Method.TO_CARD.name())).build());
        merchantMethods.add(BotDetailsRequest.MerchantMethod.builder().merchant(Merchant.ONLY_PAYS).methods(Collections.singletonList(Method.TO_CARD.name())).build());
        merchantMethods.add(BotDetailsRequest.MerchantMethod.builder().merchant(Merchant.EVO_PAY).methods(Collections.singletonList(Method.TO_CARD.name())).build());
        merchantMethods.add(BotDetailsRequest.MerchantMethod.builder().merchant(Merchant.HONEY_MONEY).methods(Collections.singletonList(Method.TO_CARD.name())).build());
        detailsRequest.setMethods(merchantMethods);
        detailsRequest.setAmount(1000);
        detailsRequest.setInitiatorApp("bot");

        OrderCreationRequest creationRequest = new OrderCreationRequest();
        creationRequest.setMethod("TO_CARD");
        creationRequest.setAmount(1000);

        List<MerchantConfig> merchantConfigs = new ArrayList<>();
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.ALFA_TEAM).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.ONLY_PAYS).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.EVO_PAY).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.HONEY_MONEY).build());
        when(merchantConfigService.findAllByMethodsAndAmount(any(), anyInt())).thenReturn(merchantConfigs);
        when(variableService.findByTypeAndConfigType(VariableType.ATTEMPTS_COUNT, ConfigType.BOT))
                .thenReturn(Variable.builder().type(VariableType.ATTEMPTS_COUNT).value("3").build());
        when(variableService.findByTypeAndConfigType(VariableType.MIN_ATTEMPT_TIME, ConfigType.BOT))
                .thenReturn(Variable.builder().type(VariableType.MIN_ATTEMPT_TIME).value("15").build());

        MerchantService alfaMerchantService = Mockito.mock(MerchantService.class);
        when(alfaMerchantService.createOrder(creationRequest)).thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.ALFA_TEAM)).thenReturn(Optional.of(alfaMerchantService));

        MerchantService onlyPaysMerchantService = Mockito.mock(MerchantService.class);
        when(onlyPaysMerchantService.createOrder(creationRequest)).thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.ONLY_PAYS)).thenReturn(Optional.of(onlyPaysMerchantService));

        MerchantService evoPayMerchantService = Mockito.mock(MerchantService.class);
        when(evoPayMerchantService.createOrder(creationRequest)).thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.EVO_PAY)).thenReturn(Optional.of(evoPayMerchantService));

        MerchantService honeyMoneyMerchantService = Mockito.mock(MerchantService.class);
        when(honeyMoneyMerchantService.createOrder(creationRequest)).thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.HONEY_MONEY)).thenReturn(Optional.of(honeyMoneyMerchantService));

        Optional<DetailsResponse> maybeDetailsResponse = merchantDetailsService.getDetails(detailsRequest);
        assertTrue(maybeDetailsResponse.isEmpty());
    }

    @Test
    void getDetailsShouldReturnDetailsIfFirstHasDetailsOnSecondIteration() {
        BotDetailsRequest detailsRequest = new BotDetailsRequest();
        List<BotDetailsRequest.MerchantMethod> merchantMethods = new ArrayList<>();
        merchantMethods.add(BotDetailsRequest.MerchantMethod.builder().merchant(Merchant.ALFA_TEAM).methods(Collections.singletonList(Method.TO_CARD.name())).build());
        merchantMethods.add(BotDetailsRequest.MerchantMethod.builder().merchant(Merchant.ONLY_PAYS).methods(Collections.singletonList(Method.TO_CARD.name())).build());
        merchantMethods.add(BotDetailsRequest.MerchantMethod.builder().merchant(Merchant.EVO_PAY).methods(Collections.singletonList(Method.TO_CARD.name())).build());
        merchantMethods.add(BotDetailsRequest.MerchantMethod.builder().merchant(Merchant.HONEY_MONEY).methods(Collections.singletonList(Method.TO_CARD.name())).build());
        detailsRequest.setMethods(merchantMethods);
        detailsRequest.setAmount(1000);
        detailsRequest.setInitiatorApp("bot");

        OrderCreationRequest creationRequest = new OrderCreationRequest();
        creationRequest.setMethod("TO_CARD");
        creationRequest.setAmount(1000);

        List<MerchantConfig> merchantConfigs = new ArrayList<>();
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.ALFA_TEAM).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.ONLY_PAYS).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.EVO_PAY).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.HONEY_MONEY).build());
        when(merchantConfigService.findAllByMethodsAndAmount(any(), anyInt())).thenReturn(merchantConfigs);
        when(variableService.findByTypeAndConfigType(VariableType.ATTEMPTS_COUNT, ConfigType.BOT))
                .thenReturn(Variable.builder().type(VariableType.ATTEMPTS_COUNT).value("3").build());
        when(variableService.findByTypeAndConfigType(VariableType.MIN_ATTEMPT_TIME, ConfigType.BOT))
                .thenReturn(Variable.builder().type(VariableType.MIN_ATTEMPT_TIME).value("15").build());

        DetailsResponse detailsResponse = new DetailsResponse();
        String expectedId = UUID.randomUUID().toString();
        detailsResponse.setMerchantOrderId(expectedId);
        detailsResponse.setMerchant(Merchant.ALFA_TEAM);

        MerchantService alfaMerchantService = Mockito.mock(MerchantService.class);
        when(alfaMerchantService.createOrder(creationRequest)).thenReturn(Optional.empty()).thenReturn(Optional.of(detailsResponse));
        when(merchantServiceRegistry.getService(Merchant.ALFA_TEAM)).thenReturn(Optional.of(alfaMerchantService));

        MerchantService onlyPaysMerchantService = Mockito.mock(MerchantService.class);
        when(onlyPaysMerchantService.createOrder(creationRequest)).thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.ONLY_PAYS)).thenReturn(Optional.of(onlyPaysMerchantService));

        MerchantService evoPayMerchantService = Mockito.mock(MerchantService.class);
        when(evoPayMerchantService.createOrder(creationRequest)).thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.EVO_PAY)).thenReturn(Optional.of(evoPayMerchantService));


        MerchantService honeyMoneyMerchantService = Mockito.mock(MerchantService.class);
        when(honeyMoneyMerchantService.createOrder(creationRequest)).thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.HONEY_MONEY)).thenReturn(Optional.of(honeyMoneyMerchantService));

        Optional<DetailsResponse> maybeDetailsResponse = merchantDetailsService.getDetails(detailsRequest);
        assertTrue(maybeDetailsResponse.isPresent());
        assertEquals(expectedId, maybeDetailsResponse.get().getMerchantOrderId());
        assertEquals(Merchant.ALFA_TEAM, maybeDetailsResponse.get().getMerchant());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    void getDetailsShouldTryAttemptsCountTimes(Integer times) {
        BotDetailsRequest detailsRequest = new BotDetailsRequest();
        List<BotDetailsRequest.MerchantMethod> merchantMethods = new ArrayList<>();
        merchantMethods.add(BotDetailsRequest.MerchantMethod.builder().merchant(Merchant.ALFA_TEAM).methods(Collections.singletonList(Method.TO_CARD.name())).build());
        merchantMethods.add(BotDetailsRequest.MerchantMethod.builder().merchant(Merchant.ONLY_PAYS).methods(Collections.singletonList(Method.TO_CARD.name())).build());
        merchantMethods.add(BotDetailsRequest.MerchantMethod.builder().merchant(Merchant.EVO_PAY).methods(Collections.singletonList(Method.TO_CARD.name())).build());
        merchantMethods.add(BotDetailsRequest.MerchantMethod.builder().merchant(Merchant.HONEY_MONEY).methods(Collections.singletonList(Method.TO_CARD.name())).build());
        detailsRequest.setMethods(merchantMethods);
        detailsRequest.setAmount(1000);
        detailsRequest.setInitiatorApp("bot");

        OrderCreationRequest creationRequest = new OrderCreationRequest();
        creationRequest.setMethod("TO_CARD");
        creationRequest.setAmount(1000);

        List<MerchantConfig> merchantConfigs = new ArrayList<>();
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.ALFA_TEAM).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.ONLY_PAYS).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.EVO_PAY).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.HONEY_MONEY).build());
        when(merchantConfigService.findAllByMethodsAndAmount(any(), anyInt())).thenReturn(merchantConfigs);
        when(variableService.findByTypeAndConfigType(VariableType.ATTEMPTS_COUNT, ConfigType.BOT))
                .thenReturn(Variable.builder().type(VariableType.ATTEMPTS_COUNT).value(String.valueOf(times)).build());
        if (times > 1) {
            when(variableService.findByTypeAndConfigType(VariableType.MIN_ATTEMPT_TIME, ConfigType.BOT))
                    .thenReturn(Variable.builder().type(VariableType.MIN_ATTEMPT_TIME).value("15").build());
        }

        MerchantService alfaMerchantService = Mockito.mock(MerchantService.class);
        when(alfaMerchantService.createOrder(creationRequest)).thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.ALFA_TEAM)).thenReturn(Optional.of(alfaMerchantService));

        MerchantService onlyPaysMerchantService = Mockito.mock(MerchantService.class);
        when(onlyPaysMerchantService.createOrder(creationRequest)).thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.ONLY_PAYS)).thenReturn(Optional.of(onlyPaysMerchantService));

        MerchantService evoPayMerchantService = Mockito.mock(MerchantService.class);
        when(evoPayMerchantService.createOrder(creationRequest)).thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.EVO_PAY)).thenReturn(Optional.of(evoPayMerchantService));


        MerchantService honeyMoneyMerchantService = Mockito.mock(MerchantService.class);
        when(honeyMoneyMerchantService.createOrder(creationRequest)).thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.HONEY_MONEY)).thenReturn(Optional.of(honeyMoneyMerchantService));

        merchantDetailsService.getDetails(detailsRequest);
        verify(alfaMerchantService, times(times)).createOrder(creationRequest);
        verify(onlyPaysMerchantService, times(times)).createOrder(creationRequest);
        verify(evoPayMerchantService, times(times)).createOrder(creationRequest);
        verify(honeyMoneyMerchantService, times(times)).createOrder(creationRequest);
    }

    @Test
    void getDetailsShouldStopTryIfDetailsFounded() {
        BotDetailsRequest detailsRequest = new BotDetailsRequest();
        List<BotDetailsRequest.MerchantMethod> merchantMethods = new ArrayList<>();
        merchantMethods.add(BotDetailsRequest.MerchantMethod.builder().merchant(Merchant.ALFA_TEAM).methods(Collections.singletonList(Method.TO_CARD.name())).build());
        merchantMethods.add(BotDetailsRequest.MerchantMethod.builder().merchant(Merchant.ONLY_PAYS).methods(Collections.singletonList(Method.TO_CARD.name())).build());
        merchantMethods.add(BotDetailsRequest.MerchantMethod.builder().merchant(Merchant.EVO_PAY).methods(Collections.singletonList(Method.TO_CARD.name())).build());
        merchantMethods.add(BotDetailsRequest.MerchantMethod.builder().merchant(Merchant.HONEY_MONEY).methods(Collections.singletonList(Method.TO_CARD.name())).build());
        detailsRequest.setMethods(merchantMethods);
        detailsRequest.setAmount(1000);
        detailsRequest.setInitiatorApp("bot");

        OrderCreationRequest creationRequest = new OrderCreationRequest();
        creationRequest.setMethod("TO_CARD");
        creationRequest.setAmount(1000);

        List<MerchantConfig> merchantConfigs = new ArrayList<>();
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.ALFA_TEAM).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.ONLY_PAYS).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.EVO_PAY).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.HONEY_MONEY).build());
        when(merchantConfigService.findAllByMethodsAndAmount(any(), anyInt())).thenReturn(merchantConfigs);
        when(variableService.findByTypeAndConfigType(VariableType.ATTEMPTS_COUNT, ConfigType.BOT))
                .thenReturn(Variable.builder().type(VariableType.ATTEMPTS_COUNT).value("3").build());
        when(variableService.findByTypeAndConfigType(VariableType.MIN_ATTEMPT_TIME, ConfigType.BOT))
                .thenReturn(Variable.builder().type(VariableType.MIN_ATTEMPT_TIME).value("15").build());

        DetailsResponse detailsResponse = new DetailsResponse();
        String expectedId = UUID.randomUUID().toString();
        detailsResponse.setMerchantOrderId(expectedId);
        detailsResponse.setMerchant(Merchant.HONEY_MONEY);

        MerchantService alfaMerchantService = Mockito.mock(MerchantService.class);
        when(alfaMerchantService.createOrder(creationRequest))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(detailsResponse));
        when(merchantServiceRegistry.getService(Merchant.ALFA_TEAM)).thenReturn(Optional.of(alfaMerchantService));

        MerchantService onlyPaysMerchantService = Mockito.mock(MerchantService.class);
        when(onlyPaysMerchantService.createOrder(creationRequest)).thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.ONLY_PAYS)).thenReturn(Optional.of(onlyPaysMerchantService));

        MerchantService evoPayMerchantService = Mockito.mock(MerchantService.class);
        when(evoPayMerchantService.createOrder(creationRequest)).thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.EVO_PAY)).thenReturn(Optional.of(evoPayMerchantService));


        MerchantService honeyMoneyMerchantService = Mockito.mock(MerchantService.class);
        when(honeyMoneyMerchantService.createOrder(creationRequest)).thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.HONEY_MONEY)).thenReturn(Optional.of(honeyMoneyMerchantService));

        merchantDetailsService.getDetails(detailsRequest);
        verify(alfaMerchantService, times(3)).createOrder(creationRequest);
        verify(onlyPaysMerchantService, times(2)).createOrder(creationRequest);
        verify(evoPayMerchantService, times(2)).createOrder(creationRequest);
        verify(honeyMoneyMerchantService, times(2)).createOrder(creationRequest);
    }

    @Test
    void getDetailsShouldKeepTryAfterExceptions() {
        BotDetailsRequest detailsRequest = new BotDetailsRequest();
        List<BotDetailsRequest.MerchantMethod> merchantMethods = new ArrayList<>();
        merchantMethods.add(BotDetailsRequest.MerchantMethod.builder().merchant(Merchant.ALFA_TEAM).methods(Collections.singletonList(Method.TO_CARD.name())).build());
        merchantMethods.add(BotDetailsRequest.MerchantMethod.builder().merchant(Merchant.ONLY_PAYS).methods(Collections.singletonList(Method.TO_CARD.name())).build());
        merchantMethods.add(BotDetailsRequest.MerchantMethod.builder().merchant(Merchant.EVO_PAY).methods(Collections.singletonList(Method.TO_CARD.name())).build());
        merchantMethods.add(BotDetailsRequest.MerchantMethod.builder().merchant(Merchant.HONEY_MONEY).methods(Collections.singletonList(Method.TO_CARD.name())).build());
        detailsRequest.setMethods(merchantMethods);
        detailsRequest.setAmount(1000);
        detailsRequest.setInitiatorApp("bot");

        OrderCreationRequest creationRequest = new OrderCreationRequest();
        creationRequest.setMethod("TO_CARD");
        creationRequest.setAmount(1000);

        List<MerchantConfig> merchantConfigs = new ArrayList<>();
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.ALFA_TEAM).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.ONLY_PAYS).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.EVO_PAY).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.HONEY_MONEY).build());
        when(merchantConfigService.findAllByMethodsAndAmount(any(), anyInt())).thenReturn(merchantConfigs);
        when(variableService.findByTypeAndConfigType(VariableType.ATTEMPTS_COUNT, ConfigType.BOT))
                .thenReturn(Variable.builder().type(VariableType.ATTEMPTS_COUNT).value("3").build());
        when(variableService.findByTypeAndConfigType(VariableType.MIN_ATTEMPT_TIME, ConfigType.BOT))
                .thenReturn(Variable.builder().type(VariableType.MIN_ATTEMPT_TIME).value("15").build());

        DetailsResponse detailsResponse = new DetailsResponse();
        String expectedId = UUID.randomUUID().toString();
        detailsResponse.setMerchantOrderId(expectedId);
        detailsResponse.setMerchant(Merchant.HONEY_MONEY);

        MerchantService alfaMerchantService = Mockito.mock(MerchantService.class);
        when(alfaMerchantService.createOrder(creationRequest))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(detailsResponse));
        when(merchantServiceRegistry.getService(Merchant.ALFA_TEAM)).thenReturn(Optional.of(alfaMerchantService));

        MerchantService onlyPaysMerchantService = Mockito.mock(MerchantService.class);
        when(onlyPaysMerchantService.createOrder(creationRequest))
                .thenThrow(RuntimeException.class)
                .thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.ONLY_PAYS)).thenReturn(Optional.of(onlyPaysMerchantService));

        MerchantService evoPayMerchantService = Mockito.mock(MerchantService.class);
        when(evoPayMerchantService.createOrder(creationRequest))
                .thenThrow(RuntimeException.class)
                .thenThrow(RuntimeException.class)
                .thenReturn(Optional.empty());
        when(merchantServiceRegistry.getService(Merchant.EVO_PAY)).thenReturn(Optional.of(evoPayMerchantService));


        MerchantService honeyMoneyMerchantService = Mockito.mock(MerchantService.class);
        when(honeyMoneyMerchantService.createOrder(creationRequest)).thenThrow(RuntimeException.class);
        when(merchantServiceRegistry.getService(Merchant.HONEY_MONEY)).thenReturn(Optional.of(honeyMoneyMerchantService));

        merchantDetailsService.getDetails(detailsRequest);
        verify(alfaMerchantService, times(3)).createOrder(creationRequest);
        verify(onlyPaysMerchantService, times(2)).createOrder(creationRequest);
        verify(evoPayMerchantService, times(2)).createOrder(creationRequest);
        verify(honeyMoneyMerchantService, times(2)).createOrder(creationRequest);
    }
}
