package net.rcetech.md.integrations.platform.viatrum;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder(alphabetic = true)
public class Request {

    private String amount;

    private Integer bankId;

    @JsonProperty("callbackURL")
    private String callbackUrl;

    private Integer currencyId;

    private String externalID;

    private Method method;
}
