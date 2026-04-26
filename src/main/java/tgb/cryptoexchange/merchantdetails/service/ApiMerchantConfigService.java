package tgb.cryptoexchange.merchantdetails.service;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.dto.ApiMerchantConfigDTO;
import tgb.cryptoexchange.merchantdetails.dto.MerchantConfigRequest;
import tgb.cryptoexchange.merchantdetails.dto.UpdateApiMerchantConfigDTO;
import tgb.cryptoexchange.merchantdetails.entity.ApiMerchantConfig;
import tgb.cryptoexchange.merchantdetails.exception.MerchantConfigNotFoundException;
import tgb.cryptoexchange.merchantdetails.repository.ApiMerchantConfigRepository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;

@Service
public class ApiMerchantConfigService {

    private static final String NOT_FOUND = " not found";

    private final ApiMerchantConfigRepository repository;

    public ApiMerchantConfigService(ApiMerchantConfigRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void init() {
        for (Merchant merchant : Merchant.values()) {
            Optional<ApiMerchantConfig> merchantConfig = getMerchantConfig(merchant);
            if (merchantConfig.isEmpty()) {
                create(merchant);
            }
        }
    }

    private void create(Merchant merchant) {
        Integer maxValue = repository.findMaxMerchantOrder();
        repository.save(
                ApiMerchantConfig.builder()
                        .isOn(false)
                        .merchant(merchant)
                        .maxAmount(5000)
                        .minAmount(1)
                        .merchantOrder(Objects.nonNull(maxValue) ? maxValue + 1 : 1)
                        .build()
        );
    }

    public Optional<ApiMerchantConfig> getMerchantConfig(Merchant merchant) {
        return repository.findBy(
                Example.of(ApiMerchantConfig.builder().merchant(merchant).build()),
                FluentQuery.FetchableFluentQuery::one
        );
    }

    public Optional<ApiMerchantConfig> getByMerchantOrder(Integer order) {
        return repository.findBy(
                Example.of(ApiMerchantConfig.builder().merchantOrder(order).build()),
                FluentQuery.FetchableFluentQuery::one
        );
    }

    public Page<ApiMerchantConfigDTO> findAll(Pageable pageable, MerchantConfigRequest request) {
        return repository.findAll(
                ((root, query, criteriaBuilder) -> criteriaBuilder.and(
                        request.toPredicates(root, criteriaBuilder).toArray(new Predicate[0])
                )),
                pageable
        ).map(ApiMerchantConfigDTO::fromEntity);
    }

    public List<ApiMerchantConfig> findAllByIsOnOrderByMerchantOrder(Boolean isOn) {
        return repository.findAllByIsOnOrderByMerchantOrder(isOn);
    }

    public List<ApiMerchantConfig> findAllByMethodsAndAmount(List<DetailsRequest.MerchantMethod> methods, Integer amount) {
        Map<Merchant, DetailsRequest.MerchantMethod> sortedMerchantMethods = methods.stream()
                .collect(Collectors.toMap(DetailsRequest.MerchantMethod::getMerchant, method -> method));
        return findAllByIsOnOrderByMerchantOrder(true).stream()
                .filter(config -> sortedMerchantMethods.containsKey(config.getMerchant()))
                .filter(config -> amount <= config.getMaxAmount() && amount >= config.getMinAmount())
                .toList();
    }

    public void delete(ApiMerchantConfig config) {
        repository.delete(config);
    }

    @Transactional
    public void changeOrder(Merchant merchant, Integer newOrder) {
        ApiMerchantConfig config = getMerchantConfig(merchant).orElseThrow(
                () -> new MerchantConfigNotFoundException("Configuration for merchant " + merchant.name() + NOT_FOUND)
        );
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

    public void changeOrder(Merchant merchant, boolean isUp) {
        ApiMerchantConfig config = getMerchantConfig(merchant).orElseThrow(
                () -> new MerchantConfigNotFoundException("Configuration for merchant " + merchant.name() + NOT_FOUND)
        );
        int currentOrder = config.getMerchantOrder();
        int maxOrder = repository.findMaxMerchantOrder();

        if ((isUp && currentOrder == 1) || (!isUp && currentOrder == maxOrder)) {
            return;
        }

        IntUnaryOperator operation = order -> isUp ? order - 1 : order + 1;
        int newOrder = operation.applyAsInt(currentOrder);
        ApiMerchantConfig otherConfig = null;
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

    @Transactional
    public void update(UpdateApiMerchantConfigDTO dto) {
        ApiMerchantConfig merchantConfig = repository.findById(dto.getId())
                .orElseThrow(() -> new MerchantConfigNotFoundException(
                        "Configuration for merchant with id" + dto.getId() + NOT_FOUND));
        if (Objects.nonNull(dto.getIsOn())) {
            merchantConfig.setIsOn(dto.getIsOn());
        }
        if (Objects.nonNull(dto.getMaxAmount())) {
            merchantConfig.setMaxAmount(dto.getMaxAmount());
        }
        if (Objects.nonNull(dto.getMinAmount())) {
            merchantConfig.setMinAmount(dto.getMinAmount());
        }
        repository.save(merchantConfig);
    }

    @Transactional
    public void deleteAllByMerchantNotExist() {
        repository.deleteAllByMerchantNotIn(List.of(Merchant.values()));
    }

    @Transactional
    public void resetMerchantOrder() {
        List<ApiMerchantConfig> configs = repository.findAll(Sort.by("merchantOrder"));
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
