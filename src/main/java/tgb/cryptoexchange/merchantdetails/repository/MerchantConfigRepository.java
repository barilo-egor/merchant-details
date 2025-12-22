package tgb.cryptoexchange.merchantdetails.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.entity.MerchantConfig;

import java.util.Collection;
import java.util.List;

@Repository
public interface MerchantConfigRepository extends JpaRepository<MerchantConfig, Long>, JpaSpecificationExecutor<MerchantConfig> {

    List<MerchantConfig> findAllByIsOnAndMerchantInOrderByMerchantOrder(Boolean isOn, Collection<Merchant> merchants);

    @Query("select max(merchantOrder) from MerchantConfig")
    Integer findMaxMerchantOrder();
}
