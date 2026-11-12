package net.rcetech.md.app.detailsapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import net.rcetech.md.app.details.DetailsRequest;
import net.rcetech.md.meta.detailsapi.enums.RequestMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiDetailsRequest implements DetailsRequest {

    private String internalId;

    private String userId;

    private Integer amount;

    private Integer waitTimeout = 60;

    private List<RequestMethod> requestMethods = new ArrayList<>();

    private UUID ownerId;

    private String initiatorApp;

    @Override
    public String getId() {
        return getInternalId();
    }

}
