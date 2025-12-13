package tgb.cryptoexchange.merchantdetails.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tgb.cryptoexchange.merchantdetails.entity.MerchantSuccessStatus;

public interface MerchantSuccessStatusRepository extends JpaRepository<MerchantSuccessStatus, Long> {
}
