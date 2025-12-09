package tgb.cryptoexchange.merchantdetails.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;
import org.springframework.data.repository.query.FluentQuery;
import tgb.cryptoexchange.merchantdetails.entity.MerchantConfig;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.repository.MerchantConfigRepository;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MerchantConfigServiceTest {

    @Mock
    private MerchantConfigRepository merchantConfigRepository;

    @InjectMocks
    private MerchantConfigService merchantConfigService;

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

    }
}