package tgb.cryptoexchange.merchantdetails.details.dto;

import lombok.Data;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;

@Data
public class RequisiteResponse {

    private Merchant merchant;

    private String requisite;

    private String merchantOrderId;

    private String merchantOrderStatus;

    private String merchantCustomId;

    private Integer amount;

    private String qr;
}
