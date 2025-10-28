package tgb.cryptoexchange.merchantdetails.exception;

/**
 * Пробрасывается при ошибках в формировании подписи.
 */
public class SignatureCreationException extends RuntimeException {
    public SignatureCreationException() {
    }

    public SignatureCreationException(String message) {
        super(message);
    }

    public SignatureCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
