package tgb.cryptoexchange.merchantdetails.service;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tgb.cryptoexchange.exception.BadRequestException;
import tgb.cryptoexchange.merchantdetails.constants.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.dto.AutoConfirmConfigDTO;
import tgb.cryptoexchange.merchantdetails.dto.MerchantConfigDTO;
import tgb.cryptoexchange.merchantdetails.dto.MerchantConfigRequest;
import tgb.cryptoexchange.merchantdetails.dto.UpdateMerchantConfigDTO;
import tgb.cryptoexchange.merchantdetails.entity.AutoConfirmConfig;
import tgb.cryptoexchange.merchantdetails.entity.MerchantConfig;
import tgb.cryptoexchange.merchantdetails.entity.MerchantSuccessStatus;
import tgb.cryptoexchange.merchantdetails.exception.MerchantConfigNotFoundException;
import tgb.cryptoexchange.merchantdetails.repository.AutoConfirmConfigRepository;
import tgb.cryptoexchange.merchantdetails.repository.MerchantConfigRepository;
import tgb.cryptoexchange.merchantdetails.repository.MerchantSuccessStatusRepository;

import java.util.*;
import java.util.function.IntUnaryOperator;
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

    public Page<MerchantConfigDTO> findAll(Pageable pageable, MerchantConfigRequest request) {
        return repository.findAll(
                ((root, query, criteriaBuilder) -> criteriaBuilder.and(
                        request.toPredicates(root, criteriaBuilder).toArray(new Predicate[0])
                )),
                pageable
        ).map(MerchantConfigDTO::fromEntity);
    }

    public List<MerchantConfig> findAllByIsOnOrderByMerchantOrder(Boolean isOn) {
        return repository.findAllByIsOnOrderByMerchantOrder(isOn);
    }

    public List<MerchantConfig> findAllByMethodsAndAmount(List<DetailsRequest.MerchantMethod> methods, Integer amount) {
        Map<Merchant, DetailsRequest.MerchantMethod> sortedMerchantMethods = methods.stream()
                .collect(Collectors.toMap(DetailsRequest.MerchantMethod::getMerchant, method -> method));
        return findAllByIsOnOrderByMerchantOrder(true).stream()
                .filter(config -> sortedMerchantMethods.containsKey(config.getMerchant()))
                .filter(config -> amount <= config.getMaxAmount() && amount >= config.getMinAmount())
                .toList();
    }

    public void delete(MerchantConfig config) {
        repository.delete(config);
    }

    public void changeOrder(Merchant merchant, boolean isUp) {
        MerchantConfig config = getMerchantConfig(merchant).orElseThrow(
                () -> new MerchantConfigNotFoundException("Configuration for merchant " + merchant.name() + NOT_FOUND)
        );
        int currentOrder = config.getMerchantOrder();
        int maxOrder = repository.findMaxMerchantOrder();

        if ((isUp && currentOrder == 1) || (!isUp && currentOrder == maxOrder)) {
            return;
        }

        IntUnaryOperator operation = order -> isUp ? order - 1 : order + 1;
        int newOrder = operation.applyAsInt(currentOrder);
        MerchantConfig otherConfig = null;
        while (otherConfig == null && newOrder > -1 && newOrder <= Merchant.values().length) {
            otherConfig = getByMerchantOrder(newOrder).orElse(null);
            if (Objects.isNull(otherConfig)) {
                newOrder = operation.applyAsInt(newOrder);
            }
        }
        if (Objects.isNull(otherConfig)) {
            throw new IllegalStateException("Config with order " + newOrder + NOT_FOUND);
        }

        otherConfig.setMerchantOrder(-1);
        repository.saveAndFlush(otherConfig);

        config.setMerchantOrder(newOrder);
        repository.saveAndFlush(config);

        otherConfig.setMerchantOrder(currentOrder);
        repository.saveAndFlush(otherConfig);
    }

    public MerchantConfig save(MerchantConfig config) {
        return repository.save(config);
    }

    @Transactional
    public void update(UpdateMerchantConfigDTO dto) {
        MerchantConfig merchantConfig = repository.findById(dto.getId())
                .orElseThrow(() -> new MerchantConfigNotFoundException("Configuration for merchant with id" + dto.getId() + NOT_FOUND));
        if (Objects.nonNull(dto.getIsOn())) {
            merchantConfig.setIsOn(dto.getIsOn());
        }
        if (Objects.nonNull(dto.getIsAutoWithdrawalOn())) {
            merchantConfig.setIsAutoWithdrawalOn(dto.getIsAutoWithdrawalOn());
        }
        if (Objects.nonNull(dto.getSuccessStatuses())) {
            List<MerchantSuccessStatus> oldStatuses = merchantConfig.getSuccessStatuses();
            merchantConfig.setSuccessStatuses(new ArrayList<>());
            merchantSuccessStatusRepository.deleteAll(oldStatuses);
            for (String successStatus : dto.getSuccessStatuses()) {
                MerchantSuccessStatus newSuccessStatus = new MerchantSuccessStatus();
                newSuccessStatus.setStatus(successStatus);
                merchantConfig.getSuccessStatuses().add(merchantSuccessStatusRepository.save(newSuccessStatus));
            }
        }
        if (Objects.nonNull(dto.getMaxAmount())) {
            merchantConfig.setMaxAmount(dto.getMaxAmount());
        }
        if (Objects.nonNull(dto.getMinAmount())) {
            merchantConfig.setMinAmount(dto.getMinAmount());
        }
        if (Objects.nonNull(dto.getGroupChatId())) {
            merchantConfig.setGroupChatId(dto.getGroupChatId());
        }
        if (Objects.nonNull(dto.getConfirmConfigs())) {
            List<AutoConfirmConfig> confirmConfigs = merchantConfig.getConfirmConfigs();
            merchantConfig.setConfirmConfigs(new ArrayList<>());
            autoConfirmConfigRepository.deleteAll(confirmConfigs);
            for (AutoConfirmConfigDTO confirmConfigDTO : dto.getConfirmConfigs()) {
                AutoConfirmConfig autoConfirmConfig = new AutoConfirmConfig();
                autoConfirmConfig.setAutoConfirmType(confirmConfigDTO.getAutoConfirmType());
                autoConfirmConfig.setDeliveryType(confirmConfigDTO.getDeliveryType());
                autoConfirmConfig.setCryptoCurrency(confirmConfigDTO.getCryptoCurrency());
                merchantConfig.getConfirmConfigs().add(autoConfirmConfigRepository.save(autoConfirmConfig));
            }
        }
        repository.save(merchantConfig);
    }

    public void deleteField(Long id, String field) {
        MerchantConfig merchantConfig = repository.findById(id)
                .orElseThrow(() -> new MerchantConfigNotFoundException("Configuration for merchant with id" + id + NOT_FOUND));
        if ("groupChatId".equals(field)) {
            merchantConfig.setGroupChatId(null);
        } else {
            throw new BadRequestException("Deleting field \"" + field + "\" unsupported.");
        }
        repository.save(merchantConfig);
    }
}
