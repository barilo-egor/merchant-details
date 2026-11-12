package net.rcetech.md.integrations.platform.yolo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Request {

    private boolean useFastPayment;

    private String value;

    private String webhookUrl;

    private String externalId;

}
