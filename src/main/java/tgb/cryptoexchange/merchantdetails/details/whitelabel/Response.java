package tgb.cryptoexchange.merchantdetails.details.whitelabel;

import lombok.Data;

import java.util.List;
import java.util.Objects;

@Data
public class Response {

    private String id;

    private Sum sum;

    private List<DealDTO> deals;

    public boolean hasRequisites() {
        return Objects.nonNull(deals) && !deals.isEmpty();
    }

    @Data
    public static class Sum {

        private String amount;
    }
}
