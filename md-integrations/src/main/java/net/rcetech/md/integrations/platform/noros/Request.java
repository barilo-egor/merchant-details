package net.rcetech.md.integrations.platform.noros;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Request {

    private String orderId;

    @JsonProperty("bankId")
    private Method paymentMethod;

    private Integer amount;

    private String clientId;
}
