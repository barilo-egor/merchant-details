package tgb.cryptoexchange.merchantdetails.entity;

import jakarta.persistence.*;
import lombok.*;
import tgb.cryptoexchange.commons.enums.Merchant;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ApiMerchantConfig implements BaseConfig {

    @Id
    @GeneratedValue
    private Long id;

    private Boolean isOn;

    @Column(unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private Merchant merchant;

    private Integer maxAmount;

    private Integer minAmount;

    @Column(unique = true, nullable = false)
    private Integer merchantOrder;

}
