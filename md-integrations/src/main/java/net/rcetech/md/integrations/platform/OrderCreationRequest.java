package net.rcetech.md.integrations.platform;

import lombok.*;
import net.rcetech.md.meta.enums.RequiredReceipt;

@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreationRequest {

    protected String requestId;

    protected String id;

    protected Integer amount;

    protected String userId;

    protected String method;

    protected String redirectUrl;

    protected String phone;

    protected RequiredReceipt requiredReceipt;

}
