package tgb.cryptoexchange.merchantdetails.details.ezepay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Request {

    @JsonProperty("m_shop")
    private Long shopId;

    @JsonProperty("m_order")
    private String order;

    @JsonProperty("m_amount")
    private Integer amount;

    @JsonProperty("m_key")
    private String key;

    @JsonProperty("m_bank")
    private Integer bank;

    @JsonProperty("m_desc")
    private String desc = "empty";

    @JsonProperty("m_referer")
    private String referer = "empty";
}
