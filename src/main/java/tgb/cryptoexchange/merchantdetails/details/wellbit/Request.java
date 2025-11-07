package tgb.cryptoexchange.merchantdetails.details.wellbit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Request {

    private Integer amount;

    @JsonProperty("credential_type")
    private String credentialType;

    @JsonProperty("bank_code")
    private String bankCode;

    @JsonProperty("credential_require")
    private String credentialRequire;

    @JsonProperty("custom_number")
    private String customNumber;

    @JsonProperty("currency")
    public String getCurrency() {
        return "RUB";
    }

    @JsonProperty("client_ip")
    public String getClientIp() {
        return "-";
    }

    @JsonProperty("client_email")
    public String getClientEmail() {
        return "-";
    }

    @JsonProperty("card_from_number")
    public String getCardFromNumber() {
        return "-";
    }

    @JsonProperty("card_from_fio")
    public String getCardFromFio() {
        return "-";
    }
}
