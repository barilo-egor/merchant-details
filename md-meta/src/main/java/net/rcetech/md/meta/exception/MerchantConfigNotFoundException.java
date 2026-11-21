package net.rcetech.md.meta.exception;

/**
 * Пробрасывается в случае, если запрошенный {@link net.rcetech.md.domain.entity.MerchantConfig} не был найден
 */
public class MerchantConfigNotFoundException extends RuntimeException {
    public MerchantConfigNotFoundException(String message) {
        super(message);
    }
}
