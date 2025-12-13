package tgb.cryptoexchange.merchantdetails.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tgb.cryptoexchange.enums.CryptoCurrency;
import tgb.cryptoexchange.enums.DeliveryType;
import tgb.cryptoexchange.merchantdetails.constants.AutoConfirmType;

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
