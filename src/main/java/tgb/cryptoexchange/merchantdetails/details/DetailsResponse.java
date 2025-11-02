package tgb.cryptoexchange.merchantdetails.details;

import lombok.Data;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;

@Data
public class DetailsResponse {

    private Merchant merchant;

    private String details;

    private String merchantOrderId;

    private String merchantOrderStatus;

    private String merchantCustomId;

    private Integer amount;

    private String qr;
}
