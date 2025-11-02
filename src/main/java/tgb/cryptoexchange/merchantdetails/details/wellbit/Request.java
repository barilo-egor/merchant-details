package tgb.cryptoexchange.merchantdetails.details.wellbit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Request {

    private final String currency = "RUB";

    private Integer amount;

    @JsonProperty("credential_type")
    private String credentialType;

    @JsonProperty("bank_code")
    private String bankCode;

    @JsonProperty("credential_require")
    private String credentialRequire;

    @JsonProperty("custom_number")
    private String customNumber;

    @JsonProperty("client_ip")
    private final String clientIp = "-";

    @JsonProperty("client_email")
    private final String clientEmail = "-";

    @JsonProperty("card_from_number")
    private final String cardFromNumber = "-";

    @JsonProperty("card_from_fio")
    private final String cardFromFio = "-";
}
