package tgb.cryptoexchange.merchantdetails.service;

import org.springframework.data.domain.Example;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.entity.MerchantConfig;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.exception.MerchantConfigNotFoundException;
import tgb.cryptoexchange.merchantdetails.repository.MerchantConfigRepository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;

@Service
public class MerchantConfigService {

    private final MerchantConfigRepository repository;

    public MerchantConfigService(MerchantConfigRepository repository) {
        this.repository = repository;
        for (Merchant merchant : Merchant.values()) {
            Optional<MerchantConfig> merchantConfig = getMerchantConfig(merchant);
            if (merchantConfig.isEmpty()) {
                create(merchant);
            }
        }
    }

    public Optional<MerchantConfig> getMerchantConfig(Merchant merchant) {
        return repository.findBy(Example.of(MerchantConfig.builder().merchant(merchant).build()), FluentQuery.FetchableFluentQuery::one);
    }

    public Optional<MerchantConfig> getByMerchantOrder(Integer order) {
        return repository.findBy(Example.of(MerchantConfig.builder().merchantOrder(order).build()), FluentQuery.FetchableFluentQuery::one);
    }

    public List<MerchantConfig> findAll() {
        return repository.findAll();
    }

    public List<MerchantConfig> findAllSortedByMerchantOrder() {
        return repository.findAllByOrderByMerchantOrder();
    }

    public List<MerchantConfig> findAllByIsOnOrderByMerchantOrder(Boolean isOn) {
        return repository.findAllByIsOnOrderByMerchantOrder(isOn);
    }

    public List<MerchantConfig> findAllByMethodsAndAmount(List<DetailsRequest.MerchantMethod> methods, Integer amount) {
        Map<Merchant, DetailsRequest.MerchantMethod> sortedMerchantMethods = methods.stream()
                .collect(Collectors.toMap(DetailsRequest.MerchantMethod::getMerchant, method -> method));
        return findAllByIsOnOrderByMerchantOrder(true).stream()
                .filter(config -> amount <= config.getMaxAmount() && amount >= config.getMinAmount())
                .filter(config -> sortedMerchantMethods.containsKey(config.getMerchant()))
                .toList();
    }

    private MerchantConfig create(Merchant merchant) {
        Integer maxValue = repository.finMaxMerchantOrder();
        return repository.save(
                MerchantConfig.builder()
                        .isOn(false)
                        .merchant(merchant)
                        .isAutoWithdrawalOn(false)
                        .maxAmount(5000)
                        .merchantOrder(Objects.nonNull(maxValue) ? maxValue + 1 : 1)
                        .build()
        );
    }

    public void delete(MerchantConfig config) {
        repository.delete(config);
    }

    @Transactional
    public void changeOrder(Merchant merchant, boolean isUp) {
        MerchantConfig config = getMerchantConfig(merchant).orElseThrow(
                () -> new MerchantConfigNotFoundException("Configuration for merchant " + merchant.name() + " not found")
        );
        int currentOrder = config.getMerchantOrder();
        int maxOrder = repository.finMaxMerchantOrder();

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
            throw new IllegalStateException("Config with order " + newOrder + " not found");
        }

        otherConfig.setMerchantOrder(-1);
        repository.saveAndFlush(otherConfig);

        config.setMerchantOrder(newOrder);
        repository.saveAndFlush(config);

        otherConfig.setMerchantOrder(currentOrder);
        repository.saveAndFlush(otherConfig);
    }

    public List<MerchantConfig> findAllByIsOn(boolean isOn) {
        return repository.findBy(Example.of(MerchantConfig.builder().isOn(isOn).build()), FluentQuery.FetchableFluentQuery::all);
    }

    public MerchantConfig save(MerchantConfig config) {
        return repository.save(config);
    }
}
