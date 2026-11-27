package net.rcetech.md.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tgb.cryptoexchange.commons.enums.Merchant;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class MerchantClientIdId implements java.io.Serializable {

    @Column(name = "client_id")
    private String clientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "merchant")
    private Merchant merchant;
}
