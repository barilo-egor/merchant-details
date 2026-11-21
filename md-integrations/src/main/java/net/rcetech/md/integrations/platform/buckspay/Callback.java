package net.rcetech.md.integrations.platform.buckspay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.rcetech.md.integrations.platform.UnwrappedCallback;
import tools.jackson.databind.annotation.JsonDeserialize;

@EqualsAndHashCode(callSuper = true)
@Data
public class Callback extends UnwrappedCallback {

    @JsonProperty("invoice_id")
    private String id;

    @JsonDeserialize(using = Status.Deserializer.class)
    @JsonProperty("state")
    private Status status;

}
