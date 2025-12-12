package tgb.cryptoexchange.merchantdetails.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;
import org.springframework.data.repository.query.FluentQuery;
import tgb.cryptoexchange.merchantdetails.constants.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.dto.UpdateMerchantConfigDTO;
import tgb.cryptoexchange.merchantdetails.entity.MerchantConfig;
import tgb.cryptoexchange.merchantdetails.entity.MerchantSuccessStatus;
import tgb.cryptoexchange.merchantdetails.exception.MerchantConfigNotFoundException;
import tgb.cryptoexchange.merchantdetails.repository.MerchantConfigRepository;
import tgb.cryptoexchange.merchantdetails.repository.MerchantSuccessStatusRepository;

import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MerchantConfigServiceTest {

    @Mock
    private MerchantConfigRepository merchantConfigRepository;

    @Mock
    private MerchantSuccessStatusRepository merchantSuccessStatusRepository;

    @InjectMocks
    private MerchantConfigService merchantConfigService;

    @Captor
    private ArgumentCaptor<Example<MerchantConfig>> merchantConfigExampleCaptor;

    @Test
    void initShouldCreateForAllMerchants() {
        when(merchantConfigRepository.findBy(
                Mockito.<Example<MerchantConfig>>any(),
                Mockito.<Function<FluentQuery.FetchableFluentQuery<MerchantConfig>, Optional<MerchantConfig>>>any()))
                .thenReturn(Optional.empty());
        when(merchantConfigRepository.findMaxMerchantOrder()).thenReturn(1);
        merchantConfigService.init();
        verify(merchantConfigRepository, times(Merchant.values().length)).findMaxMerchantOrder();
        verify(merchantConfigRepository, times(Merchant.values().length)).save(any(MerchantConfig.class));
    }

    @Test
    void initShouldNotCreate() {
        when(merchantConfigRepository.findBy(
                Mockito.<Example<MerchantConfig>>any(),
                Mockito.<Function<FluentQuery.FetchableFluentQuery<MerchantConfig>, Optional<MerchantConfig>>>any()))
                .thenReturn(Optional.of(MerchantConfig.builder().build()));
        merchantConfigService.init();
        verify(merchantConfigRepository, times(0)).findMaxMerchantOrder();
        verify(merchantConfigRepository, times(0)).save(any(MerchantConfig.class));
    }

    @Test
    void initShouldCreateForNotExistingMerchantAndNotCreateForExisting() {
        Set<Merchant> existsMerchants = new HashSet<>();
        existsMerchants.add(Merchant.ALFA_TEAM);
        existsMerchants.add(Merchant.ONLY_PAYS);
        existsMerchants.add(Merchant.EVO_PAY);
        when(merchantConfigRepository.findBy(
                argThat((ArgumentMatcher<Example<MerchantConfig>>)
                        argument -> Objects.nonNull(argument) && existsMerchants.contains(argument.getProbe().getMerchant())),
                Mockito.<Function<FluentQuery.FetchableFluentQuery<MerchantConfig>, Optional<MerchantConfig>>>any()))
                .thenReturn(Optional.of(MerchantConfig.builder().build()));
        when(merchantConfigRepository.findBy(
                argThat((ArgumentMatcher<Example<MerchantConfig>>)
                        argument -> Objects.nonNull(argument) && !existsMerchants.contains(argument.getProbe().getMerchant())),
                Mockito.<Function<FluentQuery.FetchableFluentQuery<MerchantConfig>, Optional<MerchantConfig>>>any()))
                .thenReturn(Optional.empty());
        merchantConfigService.init();
        int expected = Merchant.values().length - 3;
        verify(merchantConfigRepository, times(expected)).findMaxMerchantOrder();
        verify(merchantConfigRepository, times(expected)).save(any(MerchantConfig.class));
    }

    @Test
    void initShouldCreateDefaultMerchantConfig() {
        when(merchantConfigRepository.findBy(
                argThat((ArgumentMatcher<Example<MerchantConfig>>)
                        argument -> Objects.nonNull(argument) && Merchant.ALFA_TEAM.equals(argument.getProbe().getMerchant())),
                Mockito.<Function<FluentQuery.FetchableFluentQuery<MerchantConfig>, Optional<MerchantConfig>>>any()))
                .thenReturn(Optional.empty());
        when(merchantConfigRepository.findBy(
                argThat((ArgumentMatcher<Example<MerchantConfig>>)
                        argument -> Objects.nonNull(argument) && !Merchant.ALFA_TEAM.equals(argument.getProbe().getMerchant())),
                Mockito.<Function<FluentQuery.FetchableFluentQuery<MerchantConfig>, Optional<MerchantConfig>>>any()))
                .thenReturn(Optional.of(MerchantConfig.builder().build()));
        when(merchantConfigRepository.findMaxMerchantOrder()).thenReturn(23);
        ArgumentCaptor<MerchantConfig> merchantConfigCaptor = ArgumentCaptor.forClass(MerchantConfig.class);
        merchantConfigService.init();
        verify(merchantConfigRepository).save(merchantConfigCaptor.capture());
        MerchantConfig actual = merchantConfigCaptor.getValue();
        assertAll(
                () -> assertFalse(actual.getIsOn()),
                () -> assertEquals(Merchant.ALFA_TEAM, actual.getMerchant()),
                () -> assertFalse(actual.getIsAutoWithdrawalOn()),
                () -> assertEquals(5000, actual.getMaxAmount()),
                () -> assertEquals(1, actual.getMinAmount()),
                () -> assertEquals(24, actual.getMerchantOrder())
        );
    }

    @Test
    void initShouldCreateDefaultMerchantConfigWithFirstOrderIfMaxOrderIsNull() {
        when(merchantConfigRepository.findBy(
                argThat((ArgumentMatcher<Example<MerchantConfig>>)
                        argument -> Objects.nonNull(argument) && Merchant.ALFA_TEAM.equals(argument.getProbe().getMerchant())),
                Mockito.<Function<FluentQuery.FetchableFluentQuery<MerchantConfig>, Optional<MerchantConfig>>>any()))
                .thenReturn(Optional.empty());
        when(merchantConfigRepository.findBy(
                argThat((ArgumentMatcher<Example<MerchantConfig>>)
                        argument -> Objects.nonNull(argument) && !Merchant.ALFA_TEAM.equals(argument.getProbe().getMerchant())),
                Mockito.<Function<FluentQuery.FetchableFluentQuery<MerchantConfig>, Optional<MerchantConfig>>>any()))
                .thenReturn(Optional.of(MerchantConfig.builder().build()));
        when(merchantConfigRepository.findMaxMerchantOrder()).thenReturn(null);
        ArgumentCaptor<MerchantConfig> merchantConfigCaptor = ArgumentCaptor.forClass(MerchantConfig.class);
        merchantConfigService.init();
        verify(merchantConfigRepository).save(merchantConfigCaptor.capture());
        MerchantConfig actual = merchantConfigCaptor.getValue();
        assertAll(
                () -> assertEquals(1, actual.getMerchantOrder())
        );
    }

    @ValueSource(strings = {"ALFA_TEAM", "ONLY_PAYS"})
    @ParameterizedTest
    void getMerchantConfigShouldFindConfigOfPassedMerchant(Merchant merchant) {
        merchantConfigService.getMerchantConfig(merchant);
        verify(merchantConfigRepository).findBy(merchantConfigExampleCaptor.capture(), any());
        assertEquals(merchant, merchantConfigExampleCaptor.getValue().getProbe().getMerchant());
    }

    @ValueSource(ints = {1, 1606, 500})
    @ParameterizedTest
    void getMerchantConfigShouldFindConfigOfPassedOrder(int order) {
        merchantConfigService.getByMerchantOrder(order);
        verify(merchantConfigRepository).findBy(merchantConfigExampleCaptor.capture(), any());
        assertEquals(order, merchantConfigExampleCaptor.getValue().getProbe().getMerchantOrder());
    }

    @ValueSource(booleans = {true, false})
    @ParameterizedTest
    void findAllByIsOnOrderByMerchantOrderShouldFindByIsOn(boolean isOn) {
        merchantConfigService.findAllByIsOnOrderByMerchantOrder(isOn);
        verify(merchantConfigRepository).findAllByIsOnOrderByMerchantOrder(isOn);
    }

    @Test
    void findAllByMethodsAndAmountShouldReturnEmptyConfigsIfEmptyMethods() {
        List<MerchantConfig> merchantConfigs = new ArrayList<>();
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.ALFA_TEAM).minAmount(100).maxAmount(10000).build());
        merchantConfigs.add(MerchantConfig.builder().merchant(Merchant.EVO_PAY).minAmount(5000).maxAmount(150000).build());
        when(merchantConfigRepository.findAllByIsOnOrderByMerchantOrder(any())).thenReturn(merchantConfigs);
        assertTrue(merchantConfigService.findAllByMethodsAndAmount(new ArrayList<>(), 1).isEmpty());
    }

    @Test
    void findAllShouldReturnMatchConfigIfOneOfOneMatchToOneMethod() {
        List<MerchantConfig> merchantConfigs = new ArrayList<>();
        MerchantConfig merchantConfig = MerchantConfig.builder().merchant(Merchant.ALFA_TEAM).minAmount(100).maxAmount(10000).build();
        merchantConfigs.add(merchantConfig);
        when(merchantConfigRepository.findAllByIsOnOrderByMerchantOrder(any())).thenReturn(merchantConfigs);
        List<DetailsRequest.MerchantMethod> methods = new ArrayList<>();
        methods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.ALFA_TEAM).method("SBP").build());
        List<MerchantConfig> actual = merchantConfigService.findAllByMethodsAndAmount(methods, 5500);
        assertAll(
                () -> assertEquals(1, actual.size()),
                () -> assertEquals(merchantConfig, actual.getFirst())
        );
    }

    @Test
    void findAllShouldReturnNoConfigIfOneNotMatchToOneMethod() {
        List<MerchantConfig> merchantConfigs = new ArrayList<>();
        MerchantConfig merchantConfig = MerchantConfig.builder().merchant(Merchant.ALFA_TEAM).minAmount(7000).maxAmount(10000).build();
        merchantConfigs.add(merchantConfig);
        when(merchantConfigRepository.findAllByIsOnOrderByMerchantOrder(any())).thenReturn(merchantConfigs);
        List<DetailsRequest.MerchantMethod> methods = new ArrayList<>();
        methods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.ALFA_TEAM).method("SBP").build());
        List<MerchantConfig> actual = merchantConfigService.findAllByMethodsAndAmount(methods, 5500);
        assertAll(
                () -> assertEquals(0, actual.size())
        );
    }

    @CsvSource("""
            ALFA_TEAM,SBP
            EVO_PAY,CARD
            BIT_ZONE,PHONE
            """)
    @ParameterizedTest
    void findAllShouldReturnNoConfigIfNoOneOfFewMatch(Merchant merchant, String method) {
        List<MerchantConfig> merchantConfigs = new ArrayList<>();
        MerchantConfig merchantConfig1 = MerchantConfig.builder().merchant(Merchant.ALFA_TEAM).minAmount(7000).maxAmount(10000).build();
        MerchantConfig merchantConfig2 = MerchantConfig.builder().merchant(Merchant.EVO_PAY).minAmount(1000).maxAmount(2000).build();
        MerchantConfig merchantConfig3 = MerchantConfig.builder().merchant(Merchant.BIT_ZONE).minAmount(5501).maxAmount(10000).build();
        merchantConfigs.add(merchantConfig1);
        merchantConfigs.add(merchantConfig2);
        merchantConfigs.add(merchantConfig3);
        when(merchantConfigRepository.findAllByIsOnOrderByMerchantOrder(any())).thenReturn(merchantConfigs);
        List<DetailsRequest.MerchantMethod> methods = new ArrayList<>();
        methods.add(DetailsRequest.MerchantMethod.builder().merchant(merchant).method(method).build());
        List<MerchantConfig> actual = merchantConfigService.findAllByMethodsAndAmount(methods, 5500);
        assertAll(
                () -> assertEquals(0, actual.size())
        );
    }

    @CsvSource("""
            ALFA_TEAM,SBP
            EVO_PAY,CARD
            BIT_ZONE,PHONE
            """)
    @ParameterizedTest
    void findAllShouldReturnMatchConfigIfOneOfFewMatchToOneMethod(Merchant merchant, String method) {
        List<MerchantConfig> merchantConfigs = new ArrayList<>();
        MerchantConfig merchantConfig1 = MerchantConfig.builder().merchant(Merchant.ALFA_TEAM).minAmount(7000).maxAmount(10000).build();
        MerchantConfig merchantConfig2 = MerchantConfig.builder().merchant(Merchant.EVO_PAY).minAmount(1000).maxAmount(2000).build();
        MerchantConfig merchantConfig3 = MerchantConfig.builder().merchant(Merchant.BIT_ZONE).minAmount(5501).maxAmount(10000).build();
        merchantConfigs.add(merchantConfig1);
        merchantConfigs.add(merchantConfig2);
        merchantConfigs.add(merchantConfig3);
        when(merchantConfigRepository.findAllByIsOnOrderByMerchantOrder(any())).thenReturn(merchantConfigs);
        List<DetailsRequest.MerchantMethod> methods = new ArrayList<>();
        methods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.BIT_ZONE).method(method).build());
        List<MerchantConfig> actual = merchantConfigService.findAllByMethodsAndAmount(methods, 5501);
        assertAll(
                () -> assertEquals(1, actual.size()),
                () -> assertEquals(merchantConfig3, actual.getFirst())
        );
    }

    @CsvSource("""
            ALFA_TEAM,SBP
            EVO_PAY,CARD
            BIT_ZONE,PHONE
            """)
    @ParameterizedTest
    void findAllShouldReturnMatchConfigIfFewMatchToFewMethods(Merchant merchant, String method) {
        List<MerchantConfig> merchantConfigs = new ArrayList<>();
        MerchantConfig merchantConfig1 = MerchantConfig.builder().merchant(Merchant.ALFA_TEAM).minAmount(1000).maxAmount(10000).build();
        MerchantConfig merchantConfig2 = MerchantConfig.builder().merchant(Merchant.EVO_PAY).minAmount(1000).maxAmount(2000).build();
        MerchantConfig merchantConfig3 = MerchantConfig.builder().merchant(Merchant.BIT_ZONE).minAmount(400).maxAmount(10000).build();
        MerchantConfig merchantConfig4 = MerchantConfig.builder().merchant(Merchant.WELL_BIT).minAmount(500).maxAmount(10000).build();
        MerchantConfig merchantConfig5 = MerchantConfig.builder().merchant(Merchant.ONLY_PAYS).minAmount(1).maxAmount(10000).build();
        merchantConfigs.add(merchantConfig1);
        merchantConfigs.add(merchantConfig2);
        merchantConfigs.add(merchantConfig3);
        merchantConfigs.add(merchantConfig4);
        merchantConfigs.add(merchantConfig5);
        when(merchantConfigRepository.findAllByIsOnOrderByMerchantOrder(any())).thenReturn(merchantConfigs);
        List<DetailsRequest.MerchantMethod> methods = new ArrayList<>();
        methods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.ALFA_TEAM).method(method).build());
        methods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.BIT_ZONE).method(method).build());
        methods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.WELL_BIT).method(method).build());
        List<MerchantConfig> actual = merchantConfigService.findAllByMethodsAndAmount(methods, 1500);
        assertAll(
                () -> assertEquals(3, actual.size()),
                () -> assertEquals(merchantConfig1, actual.getFirst()),
                () -> assertEquals(merchantConfig3, actual.get(1)),
                () -> assertEquals(merchantConfig4, actual.get(2))
        );
    }

    @Test
    void deleteShouldCallDelete() {
        MerchantConfig merchantConfig = new MerchantConfig();
        merchantConfig.setMerchant(Merchant.ALFA_TEAM);
        merchantConfig.setId(505L);
        merchantConfigService.delete(merchantConfig);
        verify(merchantConfigRepository).delete(merchantConfig);
    }

    @Test
    void changeOrderShouldLowerOrderFrom1To2() {
        MerchantConfig changeOrderMerchantConfig = MerchantConfig.builder()
                .merchant(Merchant.ALFA_TEAM)
                .merchantOrder(1)
                .build();

        MerchantConfig previousMerchantConfig = MerchantConfig.builder()
                .merchant(Merchant.EVO_PAY)
                .merchantOrder(2)
                .build();

        Example<MerchantConfig> merchantExample = Example.of(
                MerchantConfig.builder().merchant(Merchant.ALFA_TEAM).build()
        );
        Example<MerchantConfig> orderExample = Example.of(
                MerchantConfig.builder().merchantOrder(2).build()
        );
        when(merchantConfigRepository.findBy(eq(merchantExample), any())).thenReturn(Optional.of(changeOrderMerchantConfig));
        when(merchantConfigRepository.findMaxMerchantOrder()).thenReturn(10);
        when(merchantConfigRepository.findBy(eq(orderExample), any())).thenReturn(Optional.of(previousMerchantConfig));

        merchantConfigService.changeOrder(Merchant.ALFA_TEAM, false);

        ArgumentCaptor<MerchantConfig> configCaptor = ArgumentCaptor.forClass(MerchantConfig.class);
        verify(merchantConfigRepository, times(3)).saveAndFlush(configCaptor.capture());
        assertAll(
                () -> assertEquals(Merchant.ALFA_TEAM, configCaptor.getAllValues().get(1).getMerchant()),
                () -> assertEquals(2, configCaptor.getAllValues().get(1).getMerchantOrder()),
                () -> assertEquals(Merchant.EVO_PAY, configCaptor.getAllValues().get(2).getMerchant()),
                () -> assertEquals(1, configCaptor.getAllValues().get(2).getMerchantOrder())
        );
    }

    @Test
    void changeOrderShouldUpOrderFrom1To2() {
        MerchantConfig changeOrderMerchantConfig = MerchantConfig.builder()
                .merchant(Merchant.ALFA_TEAM)
                .merchantOrder(4)
                .build();

        MerchantConfig previousMerchantConfig = MerchantConfig.builder()
                .merchant(Merchant.EVO_PAY)
                .merchantOrder(3)
                .build();

        Example<MerchantConfig> merchantExample = Example.of(
                MerchantConfig.builder().merchant(Merchant.ALFA_TEAM).build()
        );
        Example<MerchantConfig> orderExample = Example.of(
                MerchantConfig.builder().merchantOrder(3).build()
        );
        when(merchantConfigRepository.findBy(eq(merchantExample), any())).thenReturn(Optional.of(changeOrderMerchantConfig));
        when(merchantConfigRepository.findMaxMerchantOrder()).thenReturn(26);
        when(merchantConfigRepository.findBy(eq(orderExample), any())).thenReturn(Optional.of(previousMerchantConfig));

        merchantConfigService.changeOrder(Merchant.ALFA_TEAM, true);

        ArgumentCaptor<MerchantConfig> configCaptor = ArgumentCaptor.forClass(MerchantConfig.class);
        verify(merchantConfigRepository, times(3)).saveAndFlush(configCaptor.capture());
        assertAll(
                () -> assertEquals(Merchant.ALFA_TEAM, configCaptor.getAllValues().get(1).getMerchant()),
                () -> assertEquals(3, configCaptor.getAllValues().get(1).getMerchantOrder()),
                () -> assertEquals(Merchant.EVO_PAY, configCaptor.getAllValues().get(2).getMerchant()),
                () -> assertEquals(4, configCaptor.getAllValues().get(2).getMerchantOrder())
        );
    }

    @ValueSource(booleans = {true, false})
    @ParameterizedTest
    void getMerchantConfigShouldFindConfigOfPassedIsOn(boolean isOn) {
        merchantConfigService.findAllByIsOn(isOn);
        verify(merchantConfigRepository).findBy(merchantConfigExampleCaptor.capture(), any());
        assertEquals(isOn, merchantConfigExampleCaptor.getValue().getProbe().getIsOn());
    }

    @Test
    void saveShouldCallDelete() {
        MerchantConfig merchantConfig = new MerchantConfig();
        merchantConfig.setMerchant(Merchant.ALFA_TEAM);
        merchantConfig.setId(505L);
        merchantConfigService.save(merchantConfig);
        verify(merchantConfigRepository).save(merchantConfig);
    }

    @Test
    void updateShouldThrowMerchantConfigNotFoundExceptionIfMerchantConfigNotFound() {
        UpdateMerchantConfigDTO dto = new UpdateMerchantConfigDTO();
        dto.setId(123L);
        when(merchantConfigRepository.findById(123L)).thenReturn(Optional.empty());
        assertThrows(MerchantConfigNotFoundException.class, () -> merchantConfigService.update(dto));
    }

    @ValueSource(booleans = {true, false})
    @ParameterizedTest
    void updateShouldUpdateOnlyIsOnIfOnlyIsOnPassed(Boolean isOn) {
        UpdateMerchantConfigDTO dto = new UpdateMerchantConfigDTO();
        dto.setId(123L);
        dto.setIsOn(isOn);
        MerchantConfig merchantConfig = MerchantConfig.builder().id(123L).merchant(Merchant.ALFA_TEAM).build();
        when(merchantConfigRepository.findById(123L)).thenReturn(Optional.of(merchantConfig));
        merchantConfigService.update(dto);
        ArgumentCaptor<MerchantConfig> configCaptor = ArgumentCaptor.forClass(MerchantConfig.class);
        verify(merchantConfigRepository).save(configCaptor.capture());
        MerchantConfig actual = configCaptor.getValue();
        assertAll(
                () -> assertEquals(123L, actual.getId()),
                () -> assertEquals(isOn, actual.getIsOn()),
                () -> assertNull(actual.getIsAutoWithdrawalOn()),
                () -> assertNull(actual.getSuccessStatuses()),
                () -> assertNull(actual.getMaxAmount()),
                () -> assertNull(actual.getMinAmount()),
                () -> assertNull(actual.getGroupChatId())
        );
    }


    @ValueSource(booleans = {true, false})
    @ParameterizedTest
    void updateShouldUpdateOnlyIsAutoWithdrawalOnIfOnlyIsAutoWithdrawalOnPassed(Boolean isAutoWithdrawalOn) {
        UpdateMerchantConfigDTO dto = new UpdateMerchantConfigDTO();
        dto.setId(123L);
        dto.setIsAutoWithdrawalOn(isAutoWithdrawalOn);
        MerchantConfig merchantConfig = MerchantConfig.builder().id(123L).merchant(Merchant.ALFA_TEAM).build();
        when(merchantConfigRepository.findById(123L)).thenReturn(Optional.of(merchantConfig));
        merchantConfigService.update(dto);
        ArgumentCaptor<MerchantConfig> configCaptor = ArgumentCaptor.forClass(MerchantConfig.class);
        verify(merchantConfigRepository).save(configCaptor.capture());
        MerchantConfig actual = configCaptor.getValue();
        assertAll(
                () -> assertEquals(123L, actual.getId()),
                () -> assertEquals(isAutoWithdrawalOn, actual.getIsAutoWithdrawalOn()),
                () -> assertNull(actual.getIsOn()),
                () -> assertNull(actual.getSuccessStatuses()),
                () -> assertNull(actual.getMaxAmount()),
                () -> assertNull(actual.getMinAmount()),
                () -> assertNull(actual.getGroupChatId())
        );
    }

    @ValueSource(ints = {1000, 2500})
    @ParameterizedTest
    void updateShouldUpdateOnlyMaxAmountIfOnlyIsMaxAmountPassed(Integer maxAmount) {
        UpdateMerchantConfigDTO dto = new UpdateMerchantConfigDTO();
        dto.setId(123L);
        dto.setMaxAmount(maxAmount);
        MerchantConfig merchantConfig = MerchantConfig.builder().id(123L).merchant(Merchant.ALFA_TEAM).build();
        when(merchantConfigRepository.findById(123L)).thenReturn(Optional.of(merchantConfig));
        merchantConfigService.update(dto);
        ArgumentCaptor<MerchantConfig> configCaptor = ArgumentCaptor.forClass(MerchantConfig.class);
        verify(merchantConfigRepository).save(configCaptor.capture());
        MerchantConfig actual = configCaptor.getValue();
        assertAll(
                () -> assertEquals(123L, actual.getId()),
                () -> assertEquals(maxAmount, actual.getMaxAmount()),
                () -> assertNull(actual.getIsOn()),
                () -> assertNull(actual.getSuccessStatuses()),
                () -> assertNull(actual.getIsAutoWithdrawalOn()),
                () -> assertNull(actual.getMinAmount()),
                () -> assertNull(actual.getGroupChatId())
        );
    }

    @ValueSource(ints = {1000, 2500})
    @ParameterizedTest
    void updateShouldUpdateOnlyMinAmountIfOnlyIsMinAmountPassed(Integer minAmount) {
        UpdateMerchantConfigDTO dto = new UpdateMerchantConfigDTO();
        dto.setId(123L);
        dto.setMinAmount(minAmount);
        MerchantConfig merchantConfig = MerchantConfig.builder().id(123L).merchant(Merchant.ALFA_TEAM).build();
        when(merchantConfigRepository.findById(123L)).thenReturn(Optional.of(merchantConfig));
        merchantConfigService.update(dto);
        ArgumentCaptor<MerchantConfig> configCaptor = ArgumentCaptor.forClass(MerchantConfig.class);
        verify(merchantConfigRepository).save(configCaptor.capture());
        MerchantConfig actual = configCaptor.getValue();
        assertAll(
                () -> assertEquals(123L, actual.getId()),
                () -> assertEquals(minAmount, actual.getMinAmount()),
                () -> assertNull(actual.getIsOn()),
                () -> assertNull(actual.getSuccessStatuses()),
                () -> assertNull(actual.getIsAutoWithdrawalOn()),
                () -> assertNull(actual.getMaxAmount()),
                () -> assertNull(actual.getGroupChatId())
        );
    }

    @ValueSource(longs = {1245126626L, 23562646346L})
    @ParameterizedTest
    void updateShouldUpdateOnlyGroupChatIdIfOnlyIsGroupChatIdPassed(Long groupChatId) {
        UpdateMerchantConfigDTO dto = new UpdateMerchantConfigDTO();
        dto.setId(123L);
        dto.setGroupChatId(groupChatId);
        MerchantConfig merchantConfig = MerchantConfig.builder().id(123L).merchant(Merchant.ALFA_TEAM).build();
        when(merchantConfigRepository.findById(123L)).thenReturn(Optional.of(merchantConfig));
        merchantConfigService.update(dto);
        ArgumentCaptor<MerchantConfig> configCaptor = ArgumentCaptor.forClass(MerchantConfig.class);
        verify(merchantConfigRepository).save(configCaptor.capture());
        MerchantConfig actual = configCaptor.getValue();
        assertAll(
                () -> assertEquals(123L, actual.getId()),
                () -> assertEquals(groupChatId, actual.getGroupChatId()),
                () -> assertNull(actual.getIsOn()),
                () -> assertNull(actual.getSuccessStatuses()),
                () -> assertNull(actual.getIsAutoWithdrawalOn()),
                () -> assertNull(actual.getMaxAmount()),
                () -> assertNull(actual.getMinAmount())
        );
    }

    @Test
    void updateShouldUpdateOnlySuccessStatusesIfOnlyIsSuccessStatusesPassed() {
        UpdateMerchantConfigDTO dto = new UpdateMerchantConfigDTO();
        dto.setId(123L);
        List<String> successStatuses = new ArrayList<>();
        successStatuses.add("SUCCESS");
        successStatuses.add("COMPLETED");
        dto.setSuccessStatuses(successStatuses);
        List<MerchantSuccessStatus> merchantSuccessStatuses = new ArrayList<>();
        merchantSuccessStatuses.add(MerchantSuccessStatus.builder().status("ANOTHER_SUCCESS").build());
        merchantSuccessStatuses.add(MerchantSuccessStatus.builder().status("ANOTHER_SUCCESS2").build());
        merchantSuccessStatuses.add(MerchantSuccessStatus.builder().status("ANOTHER_COMPLETED").build());
        merchantSuccessStatuses.add(MerchantSuccessStatus.builder().status("ANOTHER_COMPLETED2").build());
        MerchantConfig merchantConfig = MerchantConfig.builder()
                .id(123L)
                .merchant(Merchant.ALFA_TEAM)
                .successStatuses(merchantSuccessStatuses)
                .build();
        when(merchantConfigRepository.findById(123L)).thenReturn(Optional.of(merchantConfig));
        ArgumentCaptor<MerchantConfig> configCaptor = ArgumentCaptor.forClass(MerchantConfig.class);
        when(merchantSuccessStatusRepository.save(argThat(status -> Objects.nonNull(status) && status.getStatus().equals("SUCCESS"))))
                .thenReturn(MerchantSuccessStatus.builder().status("SUCCESS").build());
        when(merchantSuccessStatusRepository.save(argThat(status -> Objects.nonNull(status) && status.getStatus().equals("COMPLETED"))))
                .thenReturn(MerchantSuccessStatus.builder().status("COMPLETED").build());
        merchantConfigService.update(dto);
        verify(merchantSuccessStatusRepository).deleteAll(merchantSuccessStatuses);
        verify(merchantSuccessStatusRepository, times(2)).save(any(MerchantSuccessStatus.class));
        verify(merchantConfigRepository).save(configCaptor.capture());
        MerchantConfig actual = configCaptor.getValue();
        assertNotNull(actual.getSuccessStatuses());
        assertEquals(2, actual.getSuccessStatuses().size());
        assertEquals("SUCCESS", actual.getSuccessStatuses().getFirst().getStatus());
        assertEquals("COMPLETED", actual.getSuccessStatuses().get(1).getStatus());
        assertAll(
                () -> assertEquals(123L, actual.getId()),
                () -> assertNull(actual.getIsOn()),
                () -> assertNull(actual.getIsAutoWithdrawalOn()),
                () -> assertNull(actual.getMaxAmount()),
                () -> assertNull(actual.getMinAmount()),
                () -> assertNull(actual.getGroupChatId())
        );
    }


}