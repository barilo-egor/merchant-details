package tgb.cryptoexchange.merchantdetails.entity;

import jakarta.persistence.*;
import lombok.*;
import tgb.cryptoexchange.merchantdetails.constants.Merchant;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "successStatuses")
@EqualsAndHashCode(exclude = "successStatuses")
public class MerchantConfig {

    @Id
    @GeneratedValue
    private Long id;

    private Boolean isOn;

    @Column(unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private Merchant merchant;

    private Boolean isAutoWithdrawalOn;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<MerchantSuccessStatus> successStatuses;

    private Integer maxAmount;

    private Integer minAmount;

    @Column(unique = true, nullable = false)
    private Integer merchantOrder;

    @Builder.Default
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<AutoConfirmConfig> confirmConfigs = new ArrayList<>();

    private Long groupChatId;
}
