package net.rcetech.md.integrations.platform.cube;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import tools.jackson.databind.annotation.JsonSerialize;

@Data
public class Request {

    private String amount;

    @JsonProperty("external_id")
    private String externalId;

    @JsonProperty("token")
    @JsonSerialize(using = Method.Serializer.class)
    private Method method;

    @JsonProperty("callback_url")
    private String callbackUrl;
}
