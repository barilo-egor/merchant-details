package tgb.cryptoexchange.merchantdetails.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.dto.ApiMerchantConfigDTO;
import tgb.cryptoexchange.merchantdetails.dto.UpdateApiMerchantConfigDTO;
import tgb.cryptoexchange.merchantdetails.entity.ApiMerchantConfig;
import tgb.cryptoexchange.merchantdetails.exception.MerchantConfigNotFoundException;
import tgb.cryptoexchange.merchantdetails.repository.ApiMerchantConfigRepository;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiMerchantConfigServiceTest {

    @Mock
    private ApiMerchantConfigRepository repository;

    @Spy
    private PlatformTransactionManager transactionManager = new SyncTransactionManager();

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

    private static List<ApiMerchantConfig> configsForEveryMerchant(UUID ownerId) {
        List<ApiMerchantConfig> configs = new ArrayList<>();
        int order = 1;
        for (Merchant merchant : Merchant.values()) {
            configs.add(ApiMerchantConfig.builder()
                    .merchant(merchant)
                    .merchantOrder(order++)
                    .ownerId(ownerId)
                    .isOn(false)
                    .build());
        }
        return configs;
    }

    private static void awaitThreadWaitingOnLock(Thread thread) throws InterruptedException {
        long deadlineNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadlineNanos) {
            Thread.State state = thread.getState();
            if (state == Thread.State.WAITING || state == Thread.State.TIMED_WAITING) {
                return;
            }
            TimeUnit.MILLISECONDS.sleep(1);
        }
        fail("Second thread did not enter waiting state on lock within timeout");
    }

    @Test
    void findAllAndCreateIfNotExistReturnsLoadedConfigsWithoutInsert() {
        UUID ownerId = UUID.randomUUID();
        List<ApiMerchantConfig> existing = configsForEveryMerchant(ownerId);
        when(repository.findAllByOwnerIdOrderByMerchantOrderAsc(ownerId)).thenReturn(existing);

        List<ApiMerchantConfigDTO> result = service.findAllAndCreateIfNotExist(ownerId);

        assertEquals(existing.size(), result.size());
        assertEquals(existing.get(0).getMerchant(), result.get(0).getMerchant());
        verify(repository, never()).saveAll(any());
        verify(repository, never()).save(any());
        verify(repository, never()).findMaxMerchantOrder(any());
    }

    @Test
    void findAllAndCreateIfNotExistCreatesOnlyMissingFromLoadedConfigs() {
        UUID ownerId = UUID.randomUUID();
        Merchant present = Merchant.values()[0];
        ApiMerchantConfig existing = ApiMerchantConfig.builder()
                .id(7L)
                .merchant(present)
                .merchantOrder(3)
                .ownerId(ownerId)
                .isOn(true)
                .minAmount(10)
                .maxAmount(20)
                .build();
        when(repository.findAllByOwnerIdOrderByMerchantOrderAsc(ownerId)).thenReturn(List.of(existing));
        when(repository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<ApiMerchantConfigDTO> result = service.findAllAndCreateIfNotExist(ownerId);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ApiMerchantConfig>> createdCaptor = ArgumentCaptor.forClass(List.class);
        verify(repository).findAllByOwnerIdOrderByMerchantOrderAsc(ownerId);
        verify(repository).saveAll(createdCaptor.capture());
        verify(repository, never()).save(any());
        verify(repository, never()).findMaxMerchantOrder(any());
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
                () -> assertEquals(Merchant.values().length, result.size()),
                () -> assertEquals(present, result.get(0).getMerchant()),
                () -> assertEquals(3, result.get(0).getMerchantOrder()),
                () -> assertEquals(10, result.get(0).getMinAmount()),
                () -> assertEquals(expectedMerchants.get(0), result.get(1).getMerchant()),
                () -> assertEquals(4, result.get(1).getMerchantOrder())
        );
    }

    @Test
    void findAllAndCreateIfNotExistSerializesConcurrentCreatesForSameOwner() throws Exception {
        UUID ownerId = UUID.randomUUID();
        List<ApiMerchantConfig> stored = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger activeReads = new AtomicInteger();
        AtomicBoolean readsOverlapped = new AtomicBoolean();
        CountDownLatch firstEnteredRead = new CountDownLatch(1);
        CountDownLatch allowFirstToFinishRead = new CountDownLatch(1);
        when(repository.findAllByOwnerIdOrderByMerchantOrderAsc(ownerId)).thenAnswer(invocation -> {
            if (activeReads.incrementAndGet() > 1) {
                readsOverlapped.set(true);
            }
            try {
                if (firstEnteredRead.getCount() > 0) {
                    firstEnteredRead.countDown();
                    assertTrue(allowFirstToFinishRead.await(5, TimeUnit.SECONDS));
                }
                synchronized (stored) {
                    return List.copyOf(stored);
                }
            } finally {
                activeReads.decrementAndGet();
            }
        });
        when(repository.saveAll(any())).thenAnswer(invocation -> {
            List<ApiMerchantConfig> batch = new ArrayList<>();
            for (ApiMerchantConfig config : invocation.<Iterable<ApiMerchantConfig>>getArgument(0)) {
                batch.add(config);
            }
            synchronized (stored) {
                stored.addAll(batch);
            }
            return batch;
        });
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<List<ApiMerchantConfigDTO>> first = pool.submit(
                    () -> service.findAllAndCreateIfNotExist(ownerId)
            );
            assertTrue(firstEnteredRead.await(5, TimeUnit.SECONDS));
            AtomicReference<Thread> secondThreadRef = new AtomicReference<>();
            CountDownLatch secondStarted = new CountDownLatch(1);
            Future<List<ApiMerchantConfigDTO>> second = pool.submit(() -> {
                secondThreadRef.set(Thread.currentThread());
                secondStarted.countDown();
                return service.findAllAndCreateIfNotExist(ownerId);
            });
            assertTrue(secondStarted.await(5, TimeUnit.SECONDS));
            awaitThreadWaitingOnLock(secondThreadRef.get());
            allowFirstToFinishRead.countDown();

            List<ApiMerchantConfigDTO> firstResult = first.get(5, TimeUnit.SECONDS);
            List<ApiMerchantConfigDTO> secondResult = second.get(5, TimeUnit.SECONDS);
            assertAll(
                    () -> assertFalse(readsOverlapped.get()),
                    () -> assertEquals(Merchant.values().length, stored.size()),
                    () -> assertEquals(Merchant.values().length,
                            stored.stream().map(ApiMerchantConfig::getMerchant).distinct().count()),
                    () -> assertEquals(Merchant.values().length, firstResult.size()),
                    () -> assertEquals(Merchant.values().length, secondResult.size())
            );
        } finally {
            pool.shutdownNow();
        }
        verify(repository, times(1)).saveAll(any());
    }

    @Test
    void updateAndFindAllAreSerializedForSameOwner() throws Exception {
        UUID ownerId = UUID.randomUUID();
        ApiMerchantConfig config = ApiMerchantConfig.builder()
                .id(11L).merchant(Merchant.ALFA_TEAM).merchantOrder(1).ownerId(ownerId).build();
        when(repository.findById(config.getId())).thenReturn(Optional.of(config));
        when(repository.findMaxMerchantOrder(ownerId)).thenReturn(3);
        when(repository.save(any(ApiMerchantConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CountDownLatch updateInTemporaryOrderState = new CountDownLatch(1);
        CountDownLatch allowUpdateToFinish = new CountDownLatch(1);
        when(repository.saveAndFlush(any(ApiMerchantConfig.class))).thenAnswer(invocation -> {
            updateInTemporaryOrderState.countDown();
            assertTrue(allowUpdateToFinish.await(5, TimeUnit.SECONDS));
            return invocation.getArgument(0);
        });
        AtomicInteger observedOrderDuringFindAll = new AtomicInteger();
        when(repository.findAllByOwnerIdOrderByMerchantOrderAsc(ownerId)).thenAnswer(invocation -> {
            observedOrderDuringFindAll.set(config.getMerchantOrder());
            return configsForEveryMerchant(ownerId);
        });

        UpdateApiMerchantConfigDTO updateDto = new UpdateApiMerchantConfigDTO();
        updateDto.setId(config.getId());
        updateDto.setNewOrder(2);
        updateDto.setSwap(false);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<ApiMerchantConfigDTO> updateFuture = pool.submit(() -> service.update(updateDto));
            assertTrue(updateInTemporaryOrderState.await(5, TimeUnit.SECONDS));

            AtomicReference<Thread> findAllThreadRef = new AtomicReference<>();
            CountDownLatch findAllStarted = new CountDownLatch(1);
            Future<List<ApiMerchantConfigDTO>> findAllFuture = pool.submit(() -> {
                findAllThreadRef.set(Thread.currentThread());
                findAllStarted.countDown();
                return service.findAllAndCreateIfNotExist(ownerId);
            });
            assertTrue(findAllStarted.await(5, TimeUnit.SECONDS));
            awaitThreadWaitingOnLock(findAllThreadRef.get());
            allowUpdateToFinish.countDown();

            updateFuture.get(5, TimeUnit.SECONDS);
            findAllFuture.get(5, TimeUnit.SECONDS);

            assertEquals(2, observedOrderDuringFindAll.get());
        } finally {
            pool.shutdownNow();
        }
    }

    private static final class SyncTransactionManager extends AbstractPlatformTransactionManager {

        @Override
        protected Object doGetTransaction() throws TransactionException {
            return new Object();
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
        }
    }
}
