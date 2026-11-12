package net.rcetech.md.app.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.meta.dto.ApiMerchantConfigDTO;
import net.rcetech.md.meta.dto.UpdateApiMerchantConfigDTO;
import net.rcetech.md.domain.entity.ApiMerchantConfig;
import net.rcetech.md.meta.exception.MerchantConfigNotFoundException;
import net.rcetech.md.meta.exception.ServiceUnavailableException;
import net.rcetech.md.domain.repository.ApiMerchantConfigRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiMerchantConfigServiceTest {

    @Mock
    private ApiMerchantConfigRepository repository;

    @InjectMocks
    private ApiMerchantConfigService service;

    @ParameterizedTest
    @CsvSource({
            "1, 2, 0e1f2a3b-4c5d-6e7f-8091-a2b3c4d5e6f7",
            "5, 3, 7c9a6d2e-1b3f-4a5c-8d7e-9f0a1b2c3d4e",
            "2, 7, 11111111-2222-3333-4444-555555555555"
    })
    void updateShouldSwapOrderWithConfigOnNewOrder(int currentOrder, int newOrder, String owner) {
        UUID ownerId = UUID.fromString(owner);
        ApiMerchantConfig config = ApiMerchantConfig.builder()
                .id(10L + currentOrder).merchant(Merchant.ALFA_TEAM).merchantOrder(currentOrder).ownerId(ownerId).build();
        ApiMerchantConfig other = ApiMerchantConfig.builder()
                .id(20L + newOrder).merchant(Merchant.EVO_PAY).merchantOrder(newOrder).ownerId(ownerId).build();
        when(repository.findById(config.getId())).thenReturn(Optional.of(config));
        when(repository.findByOwnerIdAndMerchantOrder(ownerId, newOrder)).thenReturn(Optional.of(other));
        when(repository.save(any(ApiMerchantConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));
        stubFindDtoById(config);
        List<Integer> flushedOrders = new ArrayList<>();
        List<Long> flushedIds = new ArrayList<>();
        when(repository.saveAndFlush(any(ApiMerchantConfig.class))).thenAnswer(invocation -> {
            ApiMerchantConfig saved = invocation.getArgument(0);
            flushedIds.add(saved.getId());
            flushedOrders.add(saved.getMerchantOrder());
            return saved;
        });
        UpdateApiMerchantConfigDTO dto = new UpdateApiMerchantConfigDTO();
        dto.setId(config.getId());
        dto.setNewOrder(newOrder);
        dto.setSwap(true);

        ApiMerchantConfigDTO result = service.update(dto);

        ArgumentCaptor<ApiMerchantConfig> savedCaptor = ArgumentCaptor.forClass(ApiMerchantConfig.class);
        InOrder inOrder = inOrder(repository);
        inOrder.verify(repository).save(savedCaptor.capture());
        inOrder.verify(repository).findDtoById(config.getId());
        assertAll(
                () -> assertEquals(List.of(config.getId(), other.getId()), flushedIds),
                () -> assertEquals(List.of(-1, currentOrder), flushedOrders),
                () -> assertSame(config, savedCaptor.getValue()),
                () -> assertEquals(newOrder, config.getMerchantOrder()),
                () -> assertEquals(currentOrder, other.getMerchantOrder()),
                () -> assertEquals(newOrder, result.merchantOrder())
        );
    }

    @ParameterizedTest
    @CsvSource({
            "1, true",
            "4, true",
            "2, false",
            "6, false"
    })
    void updateShouldNotChangeOrderIfNewOrderEqualsCurrent(int order, boolean swap) {
        UUID ownerId = UUID.randomUUID();
        ApiMerchantConfig config = ApiMerchantConfig.builder()
                .id(30L + order).merchant(Merchant.ALFA_TEAM).merchantOrder(order).ownerId(ownerId).build();
        when(repository.findById(config.getId())).thenReturn(Optional.of(config));
        when(repository.save(any(ApiMerchantConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));
        stubFindDtoById(config);
        UpdateApiMerchantConfigDTO dto = new UpdateApiMerchantConfigDTO();
        dto.setId(config.getId());
        dto.setNewOrder(order);
        dto.setSwap(swap);

        service.update(dto);

        verify(repository, never()).findMaxMerchantOrder(any());
        verify(repository, never()).findByOwnerIdAndMerchantOrder(any(), any());
        verify(repository, never()).saveAndFlush(any());
        verify(repository, never()).addOffsetToRange(any(), anyInt(), anyInt(), anyInt());
        assertEquals(order, config.getMerchantOrder());
    }

    @ParameterizedTest
    @CsvSource({
            "1, 0, 5",
            "1, -3, 5",
            "5, 6, 5",
            "5, 10, 5"
    })
    void updateShouldNotChangeOrderIfClampedNewOrderEqualsCurrent(int currentOrder, int newOrder, int maxOrder) {
        UUID ownerId = UUID.randomUUID();
        ApiMerchantConfig config = ApiMerchantConfig.builder()
                .id(30L + currentOrder).merchant(Merchant.ALFA_TEAM).merchantOrder(currentOrder).ownerId(ownerId).build();
        when(repository.findById(config.getId())).thenReturn(Optional.of(config));
        when(repository.findMaxMerchantOrder(ownerId)).thenReturn(maxOrder);
        when(repository.save(any(ApiMerchantConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));
        stubFindDtoById(config);
        UpdateApiMerchantConfigDTO dto = new UpdateApiMerchantConfigDTO();
        dto.setId(config.getId());
        dto.setNewOrder(newOrder);
        dto.setSwap(false);

        service.update(dto);

        verify(repository, never()).saveAndFlush(any());
        verify(repository, never()).addOffsetToRange(any(), anyInt(), anyInt(), anyInt());
        assertEquals(currentOrder, config.getMerchantOrder());
    }

    @ParameterizedTest
    @CsvSource({
            "2, 5, 6, , 5, 3, 5, 10006, 10009, 10011, -10007",
            "5, 2, 6, false, 2, 2, 4, 10006, 10008, 10010, -10005",
            "3, 9, 7, , 7, 4, 7, 10007, 10011, 10014, -10008",
            "4, 0, 8, false, 1, 1, 3, 10008, 10009, 10011, -10007"
    })
    void updateShouldInsertOnNewOrderIfSwapNotRequested(int currentOrder, int newOrder, int maxOrder, Boolean swap,
                                                        int expectedOrder, int firstStart, int firstEnd, int firstDelta,
                                                        int secondStart, int secondEnd, int secondDelta) {
        UUID ownerId = UUID.randomUUID();
        ApiMerchantConfig config = ApiMerchantConfig.builder()
                .id(50L + currentOrder).merchant(Merchant.ALFA_TEAM).merchantOrder(currentOrder).ownerId(ownerId).build();
        when(repository.findById(config.getId())).thenReturn(Optional.of(config));
        when(repository.findMaxMerchantOrder(ownerId)).thenReturn(maxOrder);
        when(repository.save(any(ApiMerchantConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));
        stubFindDtoById(config);
        List<Integer> flushedOrders = new ArrayList<>();
        when(repository.saveAndFlush(any(ApiMerchantConfig.class))).thenAnswer(invocation -> {
            ApiMerchantConfig saved = invocation.getArgument(0);
            flushedOrders.add(saved.getMerchantOrder());
            return saved;
        });
        UpdateApiMerchantConfigDTO dto = new UpdateApiMerchantConfigDTO();
        dto.setId(config.getId());
        dto.setNewOrder(newOrder);
        dto.setSwap(swap);

        ApiMerchantConfigDTO result = service.update(dto);

        InOrder inOrder = inOrder(repository);
        inOrder.verify(repository).addOffsetToRange(ownerId, firstStart, firstEnd, firstDelta);
        inOrder.verify(repository).addOffsetToRange(ownerId, secondStart, secondEnd, secondDelta);
        inOrder.verify(repository).save(config);
        inOrder.verify(repository).findDtoById(config.getId());
        verify(repository, never()).findByOwnerIdAndMerchantOrder(any(), any());
        assertAll(
                () -> assertEquals(List.of(-1), flushedOrders),
                () -> assertEquals(expectedOrder, config.getMerchantOrder()),
                () -> assertEquals(expectedOrder, result.merchantOrder())
        );
    }

    @ParameterizedTest
    @CsvSource({
            "1, 9",
            "3, 0"
    })
    void updateShouldThrowIfNoConfigOnNewOrder(int currentOrder, int newOrder) {
        UUID ownerId = UUID.randomUUID();
        ApiMerchantConfig config = ApiMerchantConfig.builder()
                .id(40L + currentOrder).merchant(Merchant.ALFA_TEAM).merchantOrder(currentOrder).ownerId(ownerId).build();
        when(repository.findById(config.getId())).thenReturn(Optional.of(config));
        when(repository.findByOwnerIdAndMerchantOrder(ownerId, newOrder)).thenReturn(Optional.empty());
        UpdateApiMerchantConfigDTO dto = new UpdateApiMerchantConfigDTO();
        dto.setId(config.getId());
        dto.setNewOrder(newOrder);
        dto.setSwap(true);

        assertThrows(MerchantConfigNotFoundException.class, () -> service.update(dto));
        verify(repository, never()).saveAndFlush(any());
        verify(repository, never()).save(any());
        assertEquals(currentOrder, config.getMerchantOrder());
    }

    private void stubFindDtoById(ApiMerchantConfig config) {
        when(repository.findDtoById(config.getId())).thenAnswer(invocation -> Optional.of(new ApiMerchantConfigDTO(
                config.getId(), config.getIsOn(), config.getMerchant(),
                config.getMaxAmount(), config.getMinAmount(), config.getMerchantOrder())));
    }

    private static List<ApiMerchantConfigDTO> configsForEveryMerchant() {
        List<ApiMerchantConfigDTO> configs = new ArrayList<>();
        long id = 1;
        int order = 1;
        for (Merchant merchant : Merchant.values()) {
            configs.add(new ApiMerchantConfigDTO(id++, false, merchant, 5000, 1, order++));
        }
        return configs;
    }

    @Test
    void findAllAndCreateIfNotExistReturnsLoadedConfigsWithoutInsert() {
        UUID ownerId = UUID.randomUUID();
        List<ApiMerchantConfigDTO> existing = configsForEveryMerchant();
        when(repository.findAllByOwnerIdOrderByMerchantOrderAsc(ownerId)).thenReturn(existing);

        List<ApiMerchantConfigDTO> result = service.findAllAndCreateIfNotExist(ownerId);

        assertSame(existing, result);
        verify(repository).findAllByOwnerIdOrderByMerchantOrderAsc(ownerId);
        verify(repository, never()).saveAll(any());
    }

    @Test
    void findAllAndCreateIfNotExistCreatesOnlyMissingAndRereadsConfigs() {
        UUID ownerId = UUID.randomUUID();
        Merchant present = Merchant.values()[0];
        ApiMerchantConfigDTO existing = new ApiMerchantConfigDTO(7L, true, present, 20, 10, 3);
        List<ApiMerchantConfigDTO> reread = configsForEveryMerchant();
        when(repository.findAllByOwnerIdOrderByMerchantOrderAsc(ownerId))
                .thenReturn(List.of(existing))
                .thenReturn(reread);

        List<ApiMerchantConfigDTO> result = service.findAllAndCreateIfNotExist(ownerId);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ApiMerchantConfig>> createdCaptor = ArgumentCaptor.forClass(List.class);
        verify(repository, times(2)).findAllByOwnerIdOrderByMerchantOrderAsc(ownerId);
        verify(repository).saveAll(createdCaptor.capture());
        List<ApiMerchantConfig> created = createdCaptor.getValue();
        List<Merchant> expectedMerchants = new ArrayList<>(List.of(Merchant.values()));
        expectedMerchants.remove(present);
        assertAll(
                () -> assertEquals(expectedMerchants, created.stream().map(ApiMerchantConfig::getMerchant).toList()),
                () -> assertEquals(4, created.get(0).getMerchantOrder()),
                () -> assertEquals(3 + created.size(), created.get(created.size() - 1).getMerchantOrder()),
                () -> assertTrue(created.stream().allMatch(config ->
                        ownerId.equals(config.getOwnerId())
                                && Boolean.FALSE.equals(config.getIsOn())
                                && Integer.valueOf(1).equals(config.getMinAmount())
                                && Integer.valueOf(5000).equals(config.getMaxAmount()))),
                () -> assertSame(reread, result)
        );
    }

    @Test
    void findAllAndCreateIfNotExistRereadsConfigsWhenConcurrentInsertViolatesUniqueConstraint() {
        UUID ownerId = UUID.randomUUID();
        List<ApiMerchantConfigDTO> createdByOtherInstance = configsForEveryMerchant();
        when(repository.findAllByOwnerIdOrderByMerchantOrderAsc(ownerId))
                .thenReturn(List.of())
                .thenReturn(createdByOtherInstance);
        when(repository.saveAll(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        List<ApiMerchantConfigDTO> result = service.findAllAndCreateIfNotExist(ownerId);

        verify(repository, times(2)).findAllByOwnerIdOrderByMerchantOrderAsc(ownerId);
        verify(repository).saveAll(any());
        assertSame(createdByOtherInstance, result);
    }

    @Test
    void findAllAndCreateIfNotExistThrowsWhenConfigsAreStillIncompleteAfterConstraintViolation() {
        UUID ownerId = UUID.randomUUID();
        when(repository.findAllByOwnerIdOrderByMerchantOrderAsc(ownerId)).thenReturn(List.of());
        when(repository.saveAll(any())).thenThrow(new DataIntegrityViolationException("not null"));

        assertThrows(ServiceUnavailableException.class, () -> service.findAllAndCreateIfNotExist(ownerId));

        verify(repository, times(2)).findAllByOwnerIdOrderByMerchantOrderAsc(ownerId);
    }
}
