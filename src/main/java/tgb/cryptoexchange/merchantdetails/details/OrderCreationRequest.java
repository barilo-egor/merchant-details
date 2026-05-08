package tgb.cryptoexchange.merchantdetails.details;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Builder
public class OrderCreationRequest {

    protected String requestId;

    protected String id;

    protected Integer amount;

    protected String userId;

    protected String method;

}
