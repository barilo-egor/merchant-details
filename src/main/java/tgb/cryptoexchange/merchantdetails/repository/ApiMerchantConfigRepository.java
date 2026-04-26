package tgb.cryptoexchange.merchantdetails.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.entity.ApiMerchantConfig;

import java.util.List;

@Repository
public interface ApiMerchantConfigRepository extends JpaRepository<ApiMerchantConfig, Long>, JpaSpecificationExecutor<ApiMerchantConfig> {

    List<ApiMerchantConfig> findAllByIsOnOrderByMerchantOrder(Boolean isOn);

    @Query("select max(merchantOrder) from ApiMerchantConfig")
    Integer findMaxMerchantOrder();

    @Modifying
    @Query("update ApiMerchantConfig mc set mc.merchantOrder = mc.merchantOrder + :delta " +
            "where mc.merchantOrder >= :start and mc.merchantOrder <= :end")
    void addOffsetToRange(int start, int end, int delta);

    @Modifying
    @Query("DELETE FROM ApiMerchantConfig m WHERE m.merchant NOT IN :merchants")
    @Transactional
    void deleteAllByMerchantNotIn(@Param("merchants") List<Merchant> merchants);


}
