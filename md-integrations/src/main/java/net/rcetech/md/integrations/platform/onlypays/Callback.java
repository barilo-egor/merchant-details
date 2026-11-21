package net.rcetech.md.integrations.platform.onlypays;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.rcetech.md.integrations.platform.UnwrappedCallback;
import tools.jackson.databind.annotation.JsonDeserialize;

@EqualsAndHashCode(callSuper = true)
@Data
public class Callback extends UnwrappedCallback {

    private String id;

    @JsonDeserialize(using = Status.Deserializer.class)
    private Status status;
}
