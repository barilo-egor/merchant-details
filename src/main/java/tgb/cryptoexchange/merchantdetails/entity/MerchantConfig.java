package tgb.cryptoexchange.merchantdetails.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tgb.cryptoexchange.enums.CryptoCurrency;
import tgb.cryptoexchange.enums.DeliveryType;
import tgb.cryptoexchange.merchantdetails.constants.AutoConfirmType;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;

import java.util.List;
import java.util.Optional;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantConfig {

    @Id
    @GeneratedValue
    private Long id;

    private Boolean isOn;

    @Column(unique = true, nullable = false)
    private Merchant merchant;

    private Boolean isAutoWithdrawalOn;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<MerchantSuccessStatus> successStatuses;

    private Integer maxAmount;

    private Integer minAmount;

    @Column(unique = true, nullable = false)
    private Integer merchantOrder;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<AutoConfirmConfig> confirmConfigs;

    private Integer delay;

    private Long groupChatId;

    public Optional<AutoConfirmConfig> getAutoConfirmConfig(CryptoCurrency cryptoCurrency, DeliveryType deliveryType) {
        return confirmConfigs.stream()
                .filter(conf -> cryptoCurrency.equals(conf.getCryptoCurrency())
                        && deliveryType.equals(conf.getDeliveryType()))
                .findFirst();
    }

    public Optional<AutoConfirmConfig> getAutoConfirmConfig(CryptoCurrency cryptoCurrency, DeliveryType deliveryType, AutoConfirmType autoConfirmType) {
        return confirmConfigs.stream()
                .filter(conf -> cryptoCurrency.equals(conf.getCryptoCurrency())
                        && deliveryType.equals(conf.getDeliveryType())
                && autoConfirmType.equals(conf.getAutoConfirmType()))
                .findFirst();
    }
}
