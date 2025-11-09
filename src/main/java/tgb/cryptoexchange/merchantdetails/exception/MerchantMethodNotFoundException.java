package tgb.cryptoexchange.merchantdetails.exception;

import tgb.cryptoexchange.exception.QuietException;

/**
 * Пробрасывается в случае, если при создании сделки не удалось определить метод мерчанта.
 */
public class MerchantMethodNotFoundException extends QuietException {

  public MerchantMethodNotFoundException(String message) {
    super(message);
  }
}
