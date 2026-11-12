package net.rcetech.md.meta.exception;

/**
 * Пробрасывается в случае, если при создании сделки не удалось определить метод мерчанта.
 */
public class MerchantMethodNotFoundException extends RuntimeException {

  public MerchantMethodNotFoundException(String message) {
    super(message);
  }
}
