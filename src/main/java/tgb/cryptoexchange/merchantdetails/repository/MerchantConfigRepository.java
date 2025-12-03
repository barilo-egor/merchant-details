package tgb.cryptoexchange.merchantdetails.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tgb.cryptoexchange.merchantdetails.entity.MerchantConfig;

import java.util.List;

@Repository
public interface MerchantConfigRepository extends JpaRepository<MerchantConfig, Long> {

    List<MerchantConfig> findAllByOrderByMerchantOrder();

    List<MerchantConfig> findAllByIsOnOrderByMerchantOrder(Boolean isOn);

    @Query("select max(merchantOrder) from MerchantConfig")
    Integer findMaxMerchantOrder();

    MerchantConfig findByMerchantOrder(Integer merchantOrder);

    List<MerchantConfig> findAllByIsOn(boolean isOn);
}
