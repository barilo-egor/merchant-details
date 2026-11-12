package tgb.cryptoexchange.merchantdetails.properties;

public interface YoloProperties {

    String accountId();

    String url();

    String storeKey();

    Credentials credentials();

    record Credentials(
            String login,
            String passphrase
    ) {
    }
}
