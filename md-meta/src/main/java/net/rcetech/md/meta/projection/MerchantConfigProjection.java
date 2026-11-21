package net.rcetech.md.meta.projection;

import net.rcetech.md.meta.enums.RequiredReceipt;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.util.List;

/**
 * Конфиг мерчанта для отображения.
 */
public interface MerchantConfigProjection {
    Long getId();
    Boolean getIsOn();
    Merchant getMerchant();
    Boolean getIsAutoWithdrawalOn();
    List<MerchantSuccessStatusProjection> getSuccessStatuses();
    Integer getMaxAmount();
    Integer getMinAmount();
    Integer getMerchantOrder();
    List<AutoConfirmConfigProjection> getConfirmConfigs();
    Long getGroupChatId();
    Integer getMinDealsCount();
    RequiredReceipt getRequiredReceipt();
}
