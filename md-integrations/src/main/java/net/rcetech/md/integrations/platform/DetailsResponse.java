package net.rcetech.md.integrations.platform;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.util.Objects;

@Data
public class DetailsResponse {

    private String requestId;

    private Merchant merchant;

    @JsonIgnore
    private String details;

    @JsonIgnore
    private String bank;

    private String merchantOrderId;

    private String merchantOrderStatus;

    private String merchantCustomId;

    private Integer amount;

    private String paymentMethod;

    private String qr;

    @JsonProperty("details")
    public String getFullDetails() {
        if (Objects.isNull(this.bank) && Objects.isNull(this.details)) {
            return null;
        }
        if (Objects.isNull(this.bank)) {
            return this.details;
        }
        if (Objects.isNull(this.details)) {
            return this.bank;
        }
        return this.bank + " " + this.details;
    }
}
