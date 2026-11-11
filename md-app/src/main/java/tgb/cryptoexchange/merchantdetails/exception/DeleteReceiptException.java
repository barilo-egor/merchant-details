package tgb.cryptoexchange.merchantdetails.exception;

public class DeleteReceiptException extends RuntimeException {
    public DeleteReceiptException() {
    }

    public DeleteReceiptException(String message) {
        super(message);
    }

    public DeleteReceiptException(String message, Throwable cause) {
        super(message, cause);
    }
}
