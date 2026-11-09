package tgb.cryptoexchange.merchantdetails.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.dto.ApiMerchantConfigDTO;
import tgb.cryptoexchange.merchantdetails.dto.UpdateApiMerchantConfigDTO;
import tgb.cryptoexchange.merchantdetails.entity.ApiMerchantConfig;
import tgb.cryptoexchange.merchantdetails.exception.MerchantConfigNotFoundException;
import tgb.cryptoexchange.merchantdetails.repository.ApiMerchantConfigRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        verify(repository).save(savedCaptor.capture());
        assertAll(
                () -> assertEquals(List.of(config.getId(), other.getId()), flushedIds),
                () -> assertEquals(List.of(-1, currentOrder), flushedOrders),
                () -> assertSame(config, savedCaptor.getValue()),
                () -> assertEquals(newOrder, config.getMerchantOrder()),
                () -> assertEquals(currentOrder, other.getMerchantOrder()),
                () -> assertEquals(newOrder, result.getMerchantOrder())
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
        if (!swap) {
            when(repository.findMaxMerchantOrder(ownerId)).thenReturn(order + 3);
        }
        UpdateApiMerchantConfigDTO dto = new UpdateApiMerchantConfigDTO();
        dto.setId(config.getId());
        dto.setNewOrder(order);
        dto.setSwap(swap);

        service.update(dto);

        verify(repository, never()).findByOwnerIdAndMerchantOrder(any(), any());
        verify(repository, never()).saveAndFlush(any());
        verify(repository, never()).addOffsetToRange(any(), anyInt(), anyInt(), anyInt());
        assertEquals(order, config.getMerchantOrder());
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
        verify(repository, never()).findByOwnerIdAndMerchantOrder(any(), any());
        assertAll(
                () -> assertEquals(List.of(-1), flushedOrders),
                () -> assertEquals(expectedOrder, config.getMerchantOrder()),
                () -> assertEquals(expectedOrder, result.getMerchantOrder())
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
}
