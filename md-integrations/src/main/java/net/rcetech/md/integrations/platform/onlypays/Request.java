package net.rcetech.md.integrations.platform.onlypays;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Request {

    @JsonProperty("api_id")
    private String apiId;

    @JsonProperty("secret_key")
    private String secretKey;

    @JsonProperty("amount_rub")
    private Integer amount;

    @JsonProperty("payment_type")
    private Method method;

    @JsonProperty("personal_id")
    private String personalId;

    @JsonProperty("sim")
    private Boolean sim;

    @JsonProperty
    private String bank;
}
