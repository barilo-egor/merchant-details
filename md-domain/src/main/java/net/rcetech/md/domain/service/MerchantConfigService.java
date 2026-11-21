package net.rcetech.md.domain.service;

import jakarta.annotation.PostConstruct;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.meta.dto.AutoConfirmConfigDTO;
import net.rcetech.md.meta.dto.MerchantConfigFilter;
import net.rcetech.md.meta.dto.UpdateMerchantConfigDTO;
import net.rcetech.md.domain.entity.AutoConfirmConfig;
import net.rcetech.md.domain.entity.MerchantConfig;
import net.rcetech.md.domain.entity.MerchantSuccessStatus;
import net.rcetech.md.meta.enums.RequiredReceipt;
import net.rcetech.md.meta.exception.BadRequestException;
import net.rcetech.md.meta.exception.MerchantConfigNotFoundException;
import net.rcetech.md.domain.repository.AutoConfirmConfigRepository;
import net.rcetech.md.domain.repository.MerchantConfigRepository;
import net.rcetech.md.domain.repository.MerchantConfigSpecifications;
import net.rcetech.md.domain.repository.MerchantSuccessStatusRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MerchantConfigService {

    private static final String NOT_FOUND = " not found";

    private final MerchantConfigRepository repository;

    private final MerchantSuccessStatusRepository merchantSuccessStatusRepository;

    private final AutoConfirmConfigRepository autoConfirmConfigRepository;

    public MerchantConfigService(MerchantConfigRepository repository,
                                 MerchantSuccessStatusRepository merchantSuccessStatusRepository,
                                 AutoConfirmConfigRepository autoConfirmConfigRepository) {
        this.repository = repository;
        this.merchantSuccessStatusRepository = merchantSuccessStatusRepository;
        this.autoConfirmConfigRepository = autoConfirmConfigRepository;
    }

    @PostConstruct
    public void init() {
        for (Merchant merchant : Merchant.values()) {
            Optional<MerchantConfig> merchantConfig = getMerchantConfig(merchant);
            if (merchantConfig.isEmpty()) {
                create(merchant);
            }
        }
    }

    private void create(Merchant merchant) {
        Integer maxValue = repository.findMaxMerchantOrder();
        repository.save(
                MerchantConfig.builder()
                        .isOn(false)
                        .merchant(merchant)
                        .isAutoWithdrawalOn(false)
                        .maxAmount(5000)
                        .minAmount(1)
                        .merchantOrder(Objects.nonNull(maxValue) ? maxValue + 1 : 1)
                        .minDealsCount(0)
                        .requiredReceipt(RequiredReceipt.NOT_REQUIRED)
                        .build()
        );
    }

    public Optional<MerchantConfig> getMerchantConfig(Merchant merchant) {
        return repository.findBy(
                Example.of(MerchantConfig.builder().merchant(merchant).build()),
                FluentQuery.FetchableFluentQuery::one
        );
    }

    public Optional<MerchantConfig> getByMerchantOrder(Integer order) {
        return repository.findBy(
                Example.of(MerchantConfig.builder().merchantOrder(order).build()),
                FluentQuery.FetchableFluentQuery::one
        );
    }

    public <T> Page<T> findAll(MerchantConfigFilter filter, Pageable pageable, Class<T> projectionType) {
        return repository.findBy(MerchantConfigSpecifications.matches(filter),
                query -> query.as(projectionType).page(pageable));
    }

    public List<MerchantConfig> findAllByIsOnOrderByMerchantOrder(Boolean isOn) {
        return repository.findAllByIsOnOrderByMerchantOrder(isOn);
    }

    public List<Long> findAllGroupChatIds() {
        return repository.findDistinctGroupChatIdByGroupChatIdNotNull();
    }

    public List<MerchantConfig> findAllByMerchantsAndAmount(Collection<Merchant> merchants, Integer amount) {
        return findAllByIsOnOrderByMerchantOrder(true).stream()
                .filter(config -> merchants.contains(config.getMerchant()))
                .filter(config -> amount <= config.getMaxAmount() && amount >= config.getMinAmount())
                .toList();
    }

    public void delete(MerchantConfig config) {
        repository.delete(config);
    }

    private MerchantConfig getMerchantConfigElseNotFound(Merchant merchant) {
        return getMerchantConfig(merchant).orElseThrow(
                () -> new MerchantConfigNotFoundException("Configuration for merchant " + merchant.name() + NOT_FOUND)
        );
    }

    @Transactional(readOnly = true)
    public List<Merchant> findMerchantsByMinDealsCount(Integer minDealsCount) {
        List<MerchantConfig> merchantConfigs = repository.findAllByMinDealsCountLessThanEqual(minDealsCount);
        return merchantConfigs.stream().map(MerchantConfig::getMerchant).collect(Collectors.toList());
    }

    @Transactional
    public void changeOrder(Merchant merchant, Integer newOrder) {
        MerchantConfig config = getMerchantConfigElseNotFound(merchant);
        int currentOrder = config.getMerchantOrder();
        int maxOrder = repository.findMaxMerchantOrder();
        if (currentOrder == newOrder) {
            return;
        }
        if (newOrder > maxOrder) {
            newOrder = maxOrder;
        }
        if (newOrder < 1) {
            newOrder = 1;
        }
        config.setMerchantOrder(-1);
        repository.saveAndFlush(config);

        int offset = maxOrder + 10000;
        if (newOrder > currentOrder) {
            repository.addOffsetToRange(currentOrder + 1, newOrder, offset);
            repository.addOffsetToRange(offset + currentOrder + 1, offset + newOrder, -(offset + 1));
        } else {
            repository.addOffsetToRange(newOrder, currentOrder - 1, offset);
            repository.addOffsetToRange(offset + newOrder, offset + currentOrder - 1, -(offset - 1));
        }
        config.setMerchantOrder(newOrder);
        repository.save(config);
    }

    public void changeOrder(Merchant merchantFirst, Merchant merchantSecond) {
        MerchantConfig configFirst = getMerchantConfigElseNotFound(merchantFirst);
        MerchantConfig configSecond = getMerchantConfigElseNotFound(merchantSecond);

        final Integer firstOrder = configFirst.getMerchantOrder();
        final Integer secondOrder = configSecond.getMerchantOrder();

        configFirst.setMerchantOrder(-1);
        configSecond.setMerchantOrder(-2);
        repository.saveAndFlush(configFirst);
        repository.saveAndFlush(configSecond);

        configFirst.setMerchantOrder(secondOrder);
        configSecond.setMerchantOrder(firstOrder);
        repository.saveAndFlush(configFirst);
        repository.saveAndFlush(configSecond);
    }

    public void save(MerchantConfig config) {
        repository.save(config);
    }

    @Transactional
    public void update(UpdateMerchantConfigDTO dto) {
        MerchantConfig merchantConfig = repository.findById(dto.id())
                .orElseThrow(() -> new MerchantConfigNotFoundException(
                        "Configuration for merchant with id" + dto.id() + NOT_FOUND));
        if (Objects.nonNull(dto.isOn())) {
            merchantConfig.setIsOn(dto.isOn());
        }
        if (Objects.nonNull(dto.isAutoWithdrawalOn())) {
            merchantConfig.setIsAutoWithdrawalOn(dto.isAutoWithdrawalOn());
        }
        if (Objects.nonNull(dto.successStatuses())) {
            List<MerchantSuccessStatus> oldStatuses = merchantConfig.getSuccessStatuses();
            merchantConfig.setSuccessStatuses(new ArrayList<>());
            merchantSuccessStatusRepository.deleteAll(oldStatuses);
            for (String successStatus : dto.successStatuses()) {
                MerchantSuccessStatus newSuccessStatus = new MerchantSuccessStatus();
                newSuccessStatus.setStatus(successStatus);
                merchantConfig.getSuccessStatuses().add(merchantSuccessStatusRepository.save(newSuccessStatus));
            }
        }
        if (Objects.nonNull(dto.maxAmount())) {
            merchantConfig.setMaxAmount(dto.maxAmount());
        }
        if (Objects.nonNull(dto.minAmount())) {
            merchantConfig.setMinAmount(dto.minAmount());
        }
        if (Objects.nonNull(dto.groupChatId())) {
            merchantConfig.setGroupChatId(dto.groupChatId());
        }
        if (Objects.nonNull(dto.minDealsCount())) {
            merchantConfig.setMinDealsCount(dto.minDealsCount());
        }
        if (Objects.nonNull(dto.requiredReceipt())) {
            merchantConfig.setRequiredReceipt(dto.requiredReceipt());
        }
        if (Objects.nonNull(dto.confirmConfigs())) {
            List<AutoConfirmConfig> confirmConfigs = merchantConfig.getConfirmConfigs();
            merchantConfig.setConfirmConfigs(new ArrayList<>());
            autoConfirmConfigRepository.deleteAll(confirmConfigs);
            for (AutoConfirmConfigDTO confirmConfigDTO : dto.confirmConfigs()) {
                AutoConfirmConfig autoConfirmConfig = new AutoConfirmConfig();
                autoConfirmConfig.setAutoConfirmType(confirmConfigDTO.autoConfirmType());
                autoConfirmConfig.setDeliveryType(confirmConfigDTO.deliveryType());
                autoConfirmConfig.setCryptoCurrency(confirmConfigDTO.cryptoCurrency());
                merchantConfig.getConfirmConfigs().add(autoConfirmConfigRepository.save(autoConfirmConfig));
            }
        }
        repository.save(merchantConfig);
    }

    public void deleteField(Long id, String field) {
        MerchantConfig merchantConfig = repository.findById(id)
                .orElseThrow(() -> new MerchantConfigNotFoundException(
                        "Configuration for merchant with id" + id + NOT_FOUND));
        if ("groupChatId".equals(field)) {
            merchantConfig.setGroupChatId(null);
        } else {
            throw new BadRequestException("Deleting field \"" + field + "\" unsupported.");
        }
        repository.save(merchantConfig);
    }

    @Transactional
    public void deleteAllByMerchantNotExist() {
        repository.deleteAllByMerchantNotIn(List.of(Merchant.values()));
    }

    @Transactional
    public void resetMerchantOrder() {
        List<MerchantConfig> configs = repository.findAll(Sort.by("merchantOrder"));
        if (configs.isEmpty()) {
            return;
        }
        for (int i = 0; i < configs.size(); i++) {
            configs.get(i).setMerchantOrder(-(i + 1));
        }
        repository.saveAllAndFlush(configs);
        for (int i = 0; i < configs.size(); i++) {
            configs.get(i).setMerchantOrder(i + 1);
        }
        repository.saveAll(configs);
    }


}
