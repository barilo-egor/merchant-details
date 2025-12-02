package tgb.cryptoexchange.merchantdetails.entity;

import jakarta.persistence.*;
import lombok.Data;
import tgb.cryptoexchange.enums.CryptoCurrency;
import tgb.cryptoexchange.enums.DeliveryType;
import tgb.cryptoexchange.merchantdetails.constants.AutoConfirmType;

@Entity
@Data
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
