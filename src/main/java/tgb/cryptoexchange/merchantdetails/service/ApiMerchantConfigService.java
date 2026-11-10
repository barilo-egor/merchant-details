package tgb.cryptoexchange.merchantdetails.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.dto.ApiMerchantConfigDTO;
import tgb.cryptoexchange.merchantdetails.dto.UpdateApiMerchantConfigDTO;
import tgb.cryptoexchange.merchantdetails.entity.ApiMerchantConfig;
import tgb.cryptoexchange.merchantdetails.exception.MerchantConfigNotFoundException;
import tgb.cryptoexchange.merchantdetails.exception.ServiceUnavailableException;
import tgb.cryptoexchange.merchantdetails.repository.ApiMerchantConfigRepository;

import java.util.*;

@Service
@Slf4j
public class ApiMerchantConfigService {

    private static final int DEFAULT_MIN_AMOUNT = 1;
    private static final int DEFAULT_MAX_AMOUNT = 5000;

    private final ApiMerchantConfigRepository repository;

    public ApiMerchantConfigService(ApiMerchantConfigRepository repository) {
        this.repository = repository;
    }

    public List<ApiMerchantConfigDTO> findAllAndCreateIfNotExist(UUID ownerId) {
        Objects.requireNonNull(ownerId, "ownerId must not be null");
        List<ApiMerchantConfigDTO> configs = repository.findAllByOwnerIdOrderByMerchantOrderAsc(ownerId);
        if (configs.size() == Merchant.values().length) {
            return configs;
        }
        try {
            createMissing(ownerId, configs);
        } catch (DataIntegrityViolationException e) {
            log.warn("Ошибка при создании конфигов мерчантов для ownerId={}: {}", ownerId, e.getMessage(), e);
        }
        configs = repository.findAllByOwnerIdOrderByMerchantOrderAsc(ownerId);
        if (configs.size() != Merchant.values().length) {
            long currentTime = System.currentTimeMillis();
            log.error("{} Не удалось создать конфиги мерчантов для ownerId={}", currentTime, ownerId);
            throw new ServiceUnavailableException(
                    "Error occurred while creating api merchant configs: " + currentTime + ".");
        }
        return configs;
    }

    private void createMissing(UUID ownerId, List<ApiMerchantConfigDTO> existing) {
        EnumSet<Merchant> missingMerchants = EnumSet.allOf(Merchant.class);
        int maxOrder = 0;
        for (ApiMerchantConfigDTO config : existing) {
            missingMerchants.remove(config.merchant());
            maxOrder = Math.max(maxOrder, config.merchantOrder());
        }
        int nextOrder = maxOrder + 1;
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
        repository.saveAll(toCreate);
    }

    public List<ApiMerchantConfig> findAllTurnedByMerchantsAndOwnerId(Set<Merchant> merchants, UUID ownerId) {
        return repository.findAllByMerchantInAndOwnerIdAndIsOnTrue(merchants, ownerId);
    }

    @Transactional
    public ApiMerchantConfigDTO update(UpdateApiMerchantConfigDTO dto) {
        ApiMerchantConfig merchantConfig = repository.findById(dto.getId())
                .orElseThrow(() -> new MerchantConfigNotFoundException(
                        "Configuration for merchant with id %d not found".formatted(dto.getId())));
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
        repository.save(merchantConfig);
        return repository.findDtoById(dto.getId()).orElseThrow();
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

}
