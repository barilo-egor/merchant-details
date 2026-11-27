package net.rcetech.md.meta.exception;

/**
 * Попытка включить мерчанта владельцу с другой валютой.
 * Текст ошибки показывается сотруднику в админке.
 */
public class MerchantCurrencyMismatchException extends RuntimeException {

    public MerchantCurrencyMismatchException(String message) {
        super(message);
    }
}
