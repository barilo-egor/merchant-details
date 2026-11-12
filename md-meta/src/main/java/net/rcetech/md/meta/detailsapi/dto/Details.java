package net.rcetech.md.meta.detailsapi.dto;

import lombok.Builder;
import lombok.Data;
import net.rcetech.md.meta.detailsapi.enums.RequestMethod;

@Data
@Builder
public class Details {

    private RequestMethod requestMethod;

    private String details;

    private String bank;

}
