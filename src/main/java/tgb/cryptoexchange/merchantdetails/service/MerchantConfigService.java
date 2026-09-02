package tgb.cryptoexchange.merchantdetails.service;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.exception.BadRequestException;
import tgb.cryptoexchange.merchantdetails.details.BotDetailsRequest;
import tgb.cryptoexchange.merchantdetails.dto.AutoConfirmConfigDTO;
import tgb.cryptoexchange.merchantdetails.dto.MerchantConfigDTO;
import tgb.cryptoexchange.merchantdetails.dto.MerchantConfigRequest;
import tgb.cryptoexchange.merchantdetails.dto.UpdateMerchantConfigDTO;
import tgb.cryptoexchange.merchantdetails.entity.ApiMerchantConfig;
import tgb.cryptoexchange.merchantdetails.entity.AutoConfirmConfig;
import tgb.cryptoexchange.merchantdetails.entity.MerchantConfig;
import tgb.cryptoexchange.merchantdetails.entity.MerchantSuccessStatus;
import tgb.cryptoexchange.merchantdetails.enums.RequiredReceipt;
import tgb.cryptoexchange.merchantdetails.exception.MerchantConfigNotFoundException;
import tgb.cryptoexchange.merchantdetails.repository.ApiMerchantConfigRepository;
import tgb.cryptoexchange.merchantdetails.repository.AutoConfirmConfigRepository;
import tgb.cryptoexchange.merchantdetails.repository.MerchantConfigRepository;
import tgb.cryptoexchange.merchantdetails.repository.MerchantSuccessStatusRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MerchantConfigService {

    private static final String NOT_FOUND = " not found";

    private final MerchantConfigRepository repository;

    private final ApiMerchantConfigRepository apiMerchantConfigRepository;

    private final MerchantSuccessStatusRepository merchantSuccessStatusRepository;

    private final AutoConfirmConfigRepository autoConfirmConfigRepository;

    public MerchantConfigService(MerchantConfigRepository repository, ApiMerchantConfigRepository apiMerchantConfigRepository,
                                 MerchantSuccessStatusRepository merchantSuccessStatusRepository,
                                 AutoConfirmConfigRepository autoConfirmConfigRepository) {
        this.repository = repository;
        this.apiMerchantConfigRepository = apiMerchantConfigRepository;
        this.merchantSuccessStatusRepository = merchantSuccessStatusRepository;
        this.autoConfirmConfigRepository = autoConfirmConfigRepository;
    }

    /**
     * Проверяет наличие конфигураций для всех доступных значений перечисления {@link Merchant}.
     * При старте приложения сервис {@code tgb.cryptoexchange.merchantdetails.service.StartupClearMerchantConfigService}
     * предварительно удаляет из базы данных записи конфигураций несуществующих мерчантов.
     * Если количество не совпадает, метод находит отсутствующие конфигурации и инициализирует их.
     */
    protected void checkMerchantConfigIsExist() {
        if (Merchant.values().length == countAll()) {
            return;
        }
        for (Merchant merchant : Merchant.values()) {
            Optional<MerchantConfig> merchantConfig = getMerchantConfig(merchant);
            if (merchantConfig.isEmpty()) {
                create(merchant);
            }
        }
    }

    protected void createApiConfigs(UUID ownerId) {
        for (Merchant merchant : Merchant.values()) {
            Optional<MerchantConfig> merchantConfig = getMerchantConfig(merchant);
            if (merchantConfig.isEmpty()) {
                Integer maxValue = apiMerchantConfigRepository.findMaxMerchantOrder(ownerId);
                apiMerchantConfigRepository.save(
                        ApiMerchantConfig.builder()
                                .isOn(false)
                                .merchant(merchant)
                                .maxAmount(5000)
                                .minAmount(1)
                                .merchantOrder(Objects.nonNull(maxValue) ? maxValue + 1 : 1)
                                .ownerId(ownerId)
                                .build()
                );
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

    private Long countAll() {
        return repository.count();
    }

    public List<MerchantConfigDTO> findAll(MerchantConfigRequest request) {
        List<MerchantConfig> configs = repository.findAll((root, query, criteriaBuilder) -> criteriaBuilder.and(
                        request.toPredicates(root, criteriaBuilder).toArray(new Predicate[0])
                ));
        if (Merchant.values().length != configs.size()) {
            createApiConfigs(UUID.fromString(request.getOwnerId()));
        }
         return configs.stream()
                .map(MerchantConfigDTO::fromEntity)
                .toList();
    }

    public Page<MerchantConfigDTO> findAll(Pageable pageable, MerchantConfigRequest request) {
        checkMerchantConfigIsExist();
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

    public List<Long> findAllGroupChatIds() {
        return repository.findDistinctGroupChatIdByGroupChatIdNotNull();
    }

    public List<MerchantConfig> findAllByMethodsAndAmount(List<BotDetailsRequest.MerchantMethod> methods, Integer amount) {
        checkMerchantConfigIsExist();
        Map<Merchant, BotDetailsRequest.MerchantMethod> sortedMerchantMethods = methods.stream()
                .collect(Collectors.toMap(BotDetailsRequest.MerchantMethod::getMerchant, method -> method));
        return findAllByIsOnOrderByMerchantOrder(true).stream()
                .filter(config -> sortedMerchantMethods.containsKey(config.getMerchant()))
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
    public void changeMinDealsCount(Merchant merchant, Integer minDealsCount) {
        MerchantConfig config = getMerchantConfigElseNotFound(merchant);
        config.setMinDealsCount(minDealsCount);
        repository.save(config);
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
    public MerchantConfigDTO update(UpdateMerchantConfigDTO dto) {
        MerchantConfig merchantConfig = repository.findById(dto.getId())
                .orElseThrow(() -> new MerchantConfigNotFoundException(
                        "Configuration for merchant with id" + dto.getId() + NOT_FOUND));
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
        if (Objects.nonNull(dto.getMinDealsCount())) {
            merchantConfig.setMinDealsCount(dto.getMinDealsCount());
        }
        if (Objects.nonNull(dto.getRequiredReceipt())) {
            merchantConfig.setRequiredReceipt(dto.getRequiredReceipt());
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
        MerchantConfig saved = repository.save(merchantConfig);
        return MerchantConfigDTO.fromEntity(saved);
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
