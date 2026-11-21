package net.rcetech.md.integrations.platform.settlex;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.rcetech.md.integrations.serialize.DateSerializer;
import tools.jackson.databind.annotation.JsonSerialize;

import java.time.LocalDateTime;

@Data
public class Request {

    private Integer amount;

    private String orderId;

    @JsonProperty("methodId")
    private String method;

    @JsonProperty("expired_at")
    @JsonSerialize(using = DateSerializer.ISO8601.class)
    private LocalDateTime expiredAt;

    private String callbackUri;
}
