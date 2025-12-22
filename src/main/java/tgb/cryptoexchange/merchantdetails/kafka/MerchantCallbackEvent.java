package tgb.cryptoexchange.merchantdetails.kafka;

import lombok.Data;
import tgb.cryptoexchange.commons.enums.Merchant;

@Data
public class MerchantCallbackEvent {

    private String merchantOrderId;

    private String status;

    private String statusDescription;

    private Merchant merchant;
}
