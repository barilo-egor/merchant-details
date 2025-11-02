package tgb.cryptoexchange.merchantdetails.details.pulsar;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

@Data
public class Response {

    private boolean status;

    private Result result;

    @Data
    public static class Result {

        private String id;

        @JsonDeserialize(using = Status.Deserializer.class)
        private Status state;

        private String address;

        private String bankName;
    }
}
