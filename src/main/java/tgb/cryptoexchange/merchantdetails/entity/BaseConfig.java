package tgb.cryptoexchange.merchantdetails.entity;

import tgb.cryptoexchange.commons.enums.Merchant;

public interface BaseConfig {

    Long getId();

    Merchant getMerchant();

    Boolean getIsOn();

    Integer getMaxAmount();

    Integer getMinAmount();

    Integer getMerchantOrder();

    void setMerchantOrder(Integer order);
}
