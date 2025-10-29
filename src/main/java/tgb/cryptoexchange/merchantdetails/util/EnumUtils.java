package tgb.cryptoexchange.merchantdetails.util;

import java.util.function.Supplier;

public final class EnumUtils {

    private EnumUtils() {
    }

    public static <E extends Enum<E>> E valueOf(Class<E> enumClass, String value, Supplier<RuntimeException> exceptionSupplier) {
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            throw exceptionSupplier.get();
        }
    }
}
