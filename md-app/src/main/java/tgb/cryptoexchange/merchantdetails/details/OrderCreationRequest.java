package tgb.cryptoexchange.merchantdetails.details;

import lombok.*;
import tgb.cryptoexchange.merchantdetails.enums.RequiredReceipt;

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
