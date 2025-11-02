package tgb.cryptoexchange.merchantdetails.details.way2pay;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

@Data
public class Response {

    private Boolean success;

    private Data data;

    @lombok.Data
    public static class Data {

        private String id;

        private Method method;

        private String cardNumber;

        private String phoneNumber;

        @JsonDeserialize(using = Status.Deserializer.class)
        private Status status;

        private String bank;

        private String receiver;
    }
}
