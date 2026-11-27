package net.rcetech.md.integrations.platform.tronex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.rcetech.md.integrations.platform.UnwrappedCallback;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Callback extends UnwrappedCallback {

    @JsonProperty("trade_id")
    private String id;

    private Status status;
}
