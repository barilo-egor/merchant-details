package tgb.cryptoexchange.merchantdetails.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tgb.cryptoexchange.merchantdetails.entity.AutoConfirmConfig;

public interface AutoConfirmConfigRepository extends JpaRepository<AutoConfirmConfig, Long> {
}
