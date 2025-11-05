package tgb.cryptoexchange.merchantdetails.details;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
public class ValidationResult {

    private final List<Error> errors = new ArrayList<>();

    public void notNull(String... field) {
        errors.add(new Error(String.join(" or ", field), "must not be null"));
    }

    public void expectedEmpty(String field) {
        errors.add(new Error(field, "must be empty"));
    }

    public void addError(String field, String message) {
        errors.add(new Error(field, message));
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public String errorsToString() {
        if (errors.isEmpty()) {
            return "";
        }
        return errors.stream().map(error -> "field \"" + error.getField() + "\" " + error.getMessage()).collect(Collectors.joining(";"));
    }

    @Data
    @AllArgsConstructor
    private static class Error {

        private String field;

        private String message;
    }
}
