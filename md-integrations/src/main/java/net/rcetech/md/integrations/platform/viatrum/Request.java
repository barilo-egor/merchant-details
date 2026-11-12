package net.rcetech.md.integrations.platform.viatrum;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

@Data
@JsonPropertyOrder(alphabetic = true)
public class Request {

    private String amount;

    private Integer bankId;

    @JsonProperty("callbackURL")
    private String callbackUrl;

    private Integer currencyId = 1;

    private String externalID;

    @JsonSerialize(using = Method.Serializer.class)
    @JsonDeserialize(using = Method.Deserializer.class)
    private Method method;
}
