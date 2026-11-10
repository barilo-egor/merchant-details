package tgb.cryptoexchange.merchantdetails.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.dto.ApiMerchantConfigDTO;
import tgb.cryptoexchange.merchantdetails.dto.UpdateApiMerchantConfigDTO;
import tgb.cryptoexchange.merchantdetails.entity.ApiMerchantConfig;
import tgb.cryptoexchange.merchantdetails.exception.MerchantConfigNotFoundException;
import tgb.cryptoexchange.merchantdetails.repository.ApiMerchantConfigRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Service
public class ApiMerchantConfigService {

    private static final int DEFAULT_MIN_AMOUNT = 1;
    private static final int DEFAULT_MAX_AMOUNT = 5000;

    private final ApiMerchantConfigRepository repository;
    private final TransactionTemplate transactionTemplate;
    private final ConcurrentHashMap<UUID, OwnerLock> locksByOwnerId = new ConcurrentHashMap<>();

    public ApiMerchantConfigService(ApiMerchantConfigRepository repository,
                                    PlatformTransactionManager transactionManager) {
        this.repository = repository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public List<ApiMerchantConfigDTO> findAllAndCreateIfNotExist(UUID ownerId) {
        Objects.requireNonNull(ownerId, "ownerId must not be null");
        return withOwnerLock(ownerId, () -> loadOrCreate(ownerId));
    }

    private <T> T withOwnerLock(UUID ownerId, Supplier<T> action) {
        OwnerLock lock = locksByOwnerId.compute(ownerId, (id, existing) -> {
            OwnerLock ownerLock = existing != null ? existing : new OwnerLock();
            ownerLock.usages++;
            return ownerLock;
        });
        try {
            lock.lock();
            try {
                return transactionTemplate.execute(status -> action.get());
            } finally {
                lock.unlock();
            }
        } finally {
            locksByOwnerId.computeIfPresent(ownerId, (id, existing) ->
                    --existing.usages == 0 ? null : existing);
        }
    }

    private List<ApiMerchantConfigDTO> loadOrCreate(UUID ownerId) {
        List<ApiMerchantConfig> configs = new ArrayList<>(repository.findAllByOwnerIdOrderByMerchantOrderAsc(ownerId));
        configs.addAll(createMissing(ownerId, configs));
        return configs.stream()
                .map(ApiMerchantConfigDTO::fromEntity)
                .toList();
    }

    private List<ApiMerchantConfig> createMissing(UUID ownerId, List<ApiMerchantConfig> existing) {
        EnumSet<Merchant> missingMerchants = EnumSet.allOf(Merchant.class);
        Integer maxOrder = null;
        for (ApiMerchantConfig config : existing) {
            missingMerchants.remove(config.getMerchant());
            Integer order = config.getMerchantOrder();
            if (order != null && (maxOrder == null || order > maxOrder)) {
                maxOrder = order;
            }
        }
        if (missingMerchants.isEmpty()) {
            return List.of();
        }
        int nextOrder = maxOrder == null ? 1 : maxOrder + 1;
        List<ApiMerchantConfig> toCreate = new ArrayList<>(missingMerchants.size());
        for (Merchant merchant : missingMerchants) {
            toCreate.add(ApiMerchantConfig.builder()
                    .isOn(false)
                    .merchant(merchant)
                    .maxAmount(DEFAULT_MAX_AMOUNT)
                    .minAmount(DEFAULT_MIN_AMOUNT)
                    .merchantOrder(nextOrder++)
                    .ownerId(ownerId)
                    .build());
        }
        return repository.saveAll(toCreate);
    }

    public List<ApiMerchantConfig> findAllTurnedByMerchantsAndOwnerId(Set<Merchant> merchants, UUID ownerId) {
        return repository.findAllByMerchantInAndOwnerIdAndIsOnTrue(merchants, ownerId);
    }

    public ApiMerchantConfigDTO update(UpdateApiMerchantConfigDTO dto) {
        ApiMerchantConfig existing = getByIdOrThrow(dto.getId());
        return withOwnerLock(existing.getOwnerId(), () -> updateConfig(dto));
    }

    private ApiMerchantConfig getByIdOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new MerchantConfigNotFoundException(
                        "Configuration for merchant with id %d not found".formatted(id)));
    }

    private ApiMerchantConfigDTO updateConfig(UpdateApiMerchantConfigDTO dto) {
        ApiMerchantConfig merchantConfig = getByIdOrThrow(dto.getId());
        if (Objects.nonNull(dto.getIsOn())) {
            merchantConfig.setIsOn(dto.getIsOn());
        }
        if (Objects.nonNull(dto.getMaxAmount())) {
            merchantConfig.setMaxAmount(dto.getMaxAmount());
        }
        if (Objects.nonNull(dto.getMinAmount())) {
            merchantConfig.setMinAmount(dto.getMinAmount());
        }
        if (Objects.nonNull(dto.getNewOrder())) {
            if (Boolean.TRUE.equals(dto.getSwap())) {
                swapOrder(merchantConfig, dto.getNewOrder());
            } else {
                changeOrder(merchantConfig, dto.getNewOrder());
            }
        }
        ApiMerchantConfig saved = repository.save(merchantConfig);
        return ApiMerchantConfigDTO.fromEntity(saved);
    }

    private void changeOrder(ApiMerchantConfig config, Integer newOrder) {
        int currentOrder = config.getMerchantOrder();
        if (currentOrder == newOrder) {
            return;
        }
        UUID ownerId = config.getOwnerId();
        int maxOrder = repository.findMaxMerchantOrder(ownerId);
        newOrder = Math.clamp(newOrder, 1, maxOrder);
        if (currentOrder == newOrder) {
            return;
        }
        config.setMerchantOrder(-1);
        repository.saveAndFlush(config);

        int offset = maxOrder + 10000;
        if (newOrder > currentOrder) {
            repository.addOffsetToRange(ownerId, currentOrder + 1, newOrder, offset);
            repository.addOffsetToRange(ownerId, offset + currentOrder + 1, offset + newOrder, -(offset + 1));
        } else {
            repository.addOffsetToRange(ownerId, newOrder, currentOrder - 1, offset);
            repository.addOffsetToRange(ownerId, offset + newOrder, offset + currentOrder - 1, -(offset - 1));
        }
        config.setMerchantOrder(newOrder);
    }

    private void swapOrder(ApiMerchantConfig config, Integer newOrder) {
        int currentOrder = config.getMerchantOrder();
        if (currentOrder == newOrder) {
            return;
        }
        ApiMerchantConfig other = repository.findByOwnerIdAndMerchantOrder(config.getOwnerId(), newOrder)
                .orElseThrow(() -> new MerchantConfigNotFoundException(
                        "Configuration with order %d not found".formatted(newOrder)));
        config.setMerchantOrder(-1);
        repository.saveAndFlush(config);
        other.setMerchantOrder(currentOrder);
        repository.saveAndFlush(other);
        config.setMerchantOrder(newOrder);
    }

    private static final class OwnerLock extends ReentrantLock {
        private int usages;
    }

}
