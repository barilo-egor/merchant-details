package tgb.cryptoexchange.merchantdetails.service;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Example;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.constants.MerchantConstants;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;
import tgb.cryptoexchange.merchantdetails.detailsapi.enums.RequestMethod;
import tgb.cryptoexchange.merchantdetails.dto.ApiMerchantConfigDTO;
import tgb.cryptoexchange.merchantdetails.dto.MerchantConfigRequest;
import tgb.cryptoexchange.merchantdetails.dto.UpdateApiMerchantConfigDTO;
import tgb.cryptoexchange.merchantdetails.entity.ApiMerchantConfig;
import tgb.cryptoexchange.merchantdetails.exception.MerchantConfigNotFoundException;
import tgb.cryptoexchange.merchantdetails.repository.ApiMerchantConfigRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApiMerchantConfigService {

    private static final String NOT_FOUND = " not found";

    private final ApiMerchantConfigRepository repository;

    public ApiMerchantConfigService(ApiMerchantConfigRepository repository) {
        this.repository = repository;
    }

    public List<ApiMerchantConfigDTO> findAll(MerchantConfigRequest request) {
        List<ApiMerchantConfig> configs = repository.findAll((root, query, criteriaBuilder) -> criteriaBuilder.and(
                request.toPredicates(root, criteriaBuilder).toArray(new Predicate[0])
        ));
        if (Merchant.values().length != configs.size()) {
            createApiConfigs(UUID.fromString(request.getOwnerId()));
        }
        return configs.stream()
                .map(ApiMerchantConfigDTO::fromEntity)
                .toList();
    }

    protected void createApiConfigs(UUID ownerId) {
        for (Merchant merchant : Merchant.values()) {
            Optional<ApiMerchantConfig> merchantConfig = getMerchantConfig(ownerId);
            if (merchantConfig.isEmpty()) {
                Integer maxValue = repository.findMaxMerchantOrder(ownerId);
                repository.save(
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

    public Optional<ApiMerchantConfig> getMerchantConfig(UUID ownerId) {
        return repository.findBy(
                Example.of(ApiMerchantConfig.builder().ownerId(ownerId).build()),
                FluentQuery.FetchableFluentQuery::one
        );
    }

    public List<ApiMerchantConfig> findAllByIsOnOrderByMerchantOrder(Boolean isOn) {
        return repository.findAllByIsOnOrderByMerchantOrder(isOn);
    }

    public List<ApiMerchantConfig> findAllByMethodsAndAmount(List<RequestMethod> requestMethods, Integer amount) {
        Set<Merchant> sortedMerchantsByMethod = new HashSet<>();
        Set<String> requestMethodsSet = requestMethods.stream()
                .map(RequestMethod::name)
                .collect(Collectors.toSet());
        Arrays.stream(Merchant.values()).forEach(merchant -> {
            List<MerchantMethod> merchantMethods = MerchantConstants.getMethods(merchant);
            sortMerchantByRequestMethod(merchant, merchantMethods, sortedMerchantsByMethod, requestMethodsSet);
        });


        return findAllByIsOnOrderByMerchantOrder(true).stream()
                .filter(config -> sortedMerchantsByMethod.contains(config.getMerchant()))
                .filter(config -> amount <= config.getMaxAmount() && amount >= config.getMinAmount())
                .toList();
    }

    private void sortMerchantByRequestMethod(Merchant merchant, List<MerchantMethod> merchantMethods,
                                             Set<Merchant> merchantsByMethod, Set<String> requestMethodNames) {
        merchantMethods.forEach(method -> {
            if (requestMethodNames.contains(method.name())) {
                merchantsByMethod.add(merchant);
            }
        });
    }

    @Transactional
    public ApiMerchantConfigDTO update(UpdateApiMerchantConfigDTO dto) {
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
        ApiMerchantConfig saved = repository.save(merchantConfig);
        return ApiMerchantConfigDTO.fromEntity(saved);
    }

}
