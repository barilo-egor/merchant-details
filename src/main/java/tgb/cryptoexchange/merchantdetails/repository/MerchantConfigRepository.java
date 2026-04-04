package tgb.cryptoexchange.merchantdetails.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tgb.cryptoexchange.merchantdetails.entity.MerchantConfig;

import java.util.List;

@Repository
public interface MerchantConfigRepository extends JpaRepository<MerchantConfig, Long>, JpaSpecificationExecutor<MerchantConfig> {

    List<MerchantConfig> findAllByIsOnOrderByMerchantOrder(Boolean isOn);

    @Query("select max(merchantOrder) from MerchantConfig")
    Integer findMaxMerchantOrder();

    List<Long> findDistinctGroupChatIdByGroupChatIdNotNull();

    @Modifying
    @Query("update MerchantConfig mc set mc.merchantOrder = mc.merchantOrder + :delta " +
            "where mc.merchantOrder >= :start and mc.merchantOrder <= :end")
    void addOffsetToRange(int start, int end, int delta);
}
