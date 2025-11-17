package tgb.cryptoexchange.merchantdetails.kafka;

import lombok.Data;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;

@Data
public class MerchantCallbackEvent {

    private String merchantOrderId;

    private String status;

    private Merchant merchant;
}
