package tgb.cryptoexchange.merchantdetails.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;

import java.util.List;

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

    private Long groupChatId;
}
