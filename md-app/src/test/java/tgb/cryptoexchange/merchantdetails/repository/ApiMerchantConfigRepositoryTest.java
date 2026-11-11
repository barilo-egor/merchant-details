package tgb.cryptoexchange.merchantdetails.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;
import tgb.cryptoexchange.merchantdetails.dto.ApiMerchantConfigDTO;
import tgb.cryptoexchange.merchantdetails.entity.ApiMerchantConfig;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
class ApiMerchantConfigRepositoryTest extends AbstractMySqlTest {

    @Autowired
    private ApiMerchantConfigRepository repository;

    @Test
    void findAllByOwnerIdOrderByMerchantOrderAscShouldProjectOwnerConfigsToDtoSortedByOrder() {
        UUID ownerId = UUID.randomUUID();
        ApiMerchantConfig second = repository.save(config(ownerId, Merchant.values()[0], 2, true, 10, 20));
        ApiMerchantConfig first = repository.save(config(ownerId, Merchant.values()[1], 1, false, 1, 5000));
        repository.save(config(UUID.randomUUID(), Merchant.values()[2], 1, true, 1, 5000));

        List<ApiMerchantConfigDTO> result = repository.findAllByOwnerIdOrderByMerchantOrderAsc(ownerId);

        assertEquals(List.of(toDto(first), toDto(second)), result);
    }

    @Test
    void findDtoByIdShouldProjectConfigToDto() {
        ApiMerchantConfig saved = repository.save(config(UUID.randomUUID(), Merchant.values()[0], 3, true, 10, 20));

        assertEquals(Optional.of(toDto(saved)), repository.findDtoById(saved.getId()));
    }

    @Test
    void findDtoByIdShouldReturnEmptyIfConfigNotExists() {
        assertEquals(Optional.empty(), repository.findDtoById(Long.MAX_VALUE));
    }

    private static ApiMerchantConfigDTO toDto(ApiMerchantConfig config) {
        return new ApiMerchantConfigDTO(config.getId(), config.getIsOn(), config.getMerchant(),
                config.getMaxAmount(), config.getMinAmount(), config.getMerchantOrder());
    }

    private static ApiMerchantConfig config(UUID ownerId, Merchant merchant, int order, boolean isOn,
                                            int minAmount, int maxAmount) {
        return ApiMerchantConfig.builder()
                .ownerId(ownerId)
                .merchant(merchant)
                .merchantOrder(order)
                .isOn(isOn)
                .minAmount(minAmount)
                .maxAmount(maxAmount)
                .build();
    }
}
