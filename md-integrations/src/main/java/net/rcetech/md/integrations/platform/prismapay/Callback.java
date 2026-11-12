package net.rcetech.md.integrations.platform.prismapay;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.rcetech.md.integrations.platform.UnwrappedCallback;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Callback extends UnwrappedCallback {

    private String id;

    private Status status;

    @Override
    @JsonIgnore
    public boolean isIgnored() {
        return Status.NO_REQUISITES.equals(status);
    }
}
