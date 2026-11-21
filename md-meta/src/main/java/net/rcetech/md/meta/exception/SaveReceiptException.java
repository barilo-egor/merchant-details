package net.rcetech.md.meta.exception;

public class SaveReceiptException extends RuntimeException {
    public SaveReceiptException() {
    }

    public SaveReceiptException(String message) {
        super(message);
    }

    public SaveReceiptException(String message, Throwable cause) {
        super(message, cause);
    }
}
