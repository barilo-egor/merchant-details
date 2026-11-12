package net.rcetech.md.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tgb.cryptoexchange.commons.enums.CryptoCurrency;
import tgb.cryptoexchange.commons.enums.DeliveryType;
import net.rcetech.md.meta.constants.AutoConfirmType;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AutoConfirmConfig {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private CryptoCurrency cryptoCurrency;

    @Enumerated(EnumType.STRING)
    private AutoConfirmType autoConfirmType;

    @Enumerated(EnumType.STRING)
    private DeliveryType deliveryType;
}
