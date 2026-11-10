package tgb.cryptoexchange.merchantdetails.entity;

import jakarta.persistence.*;
import lombok.*;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.util.UUID;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uc_api_merchant_config_owner_id_merchant",
                columnNames = {"owner_id", "merchant"}
        )
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ApiMerchantConfig implements BaseConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean isOn;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Merchant merchant;

    private Integer maxAmount;

    private Integer minAmount;

    @Column(nullable = false)
    private Integer merchantOrder;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

}
