package tgb.cryptoexchange.merchantdetails.details;

import lombok.*;

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

}
