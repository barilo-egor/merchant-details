package tgb.cryptoexchange.merchantdetails.util;

import org.hashids.Hashids;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

public final class ClientIdUtils {

    private ClientIdUtils() {
    }

    /**
     * Хэширует идентификатор пользователя для передачи мерчанту.
     * Числовой идентификатор кодируется как число, остальные строки — по байтам UTF-8.
     *
     * @param hashids кодировщик мерчанта
     * @param userId  идентификатор пользователя
     * @return хэш идентификатора
     */
    public static String encode(Hashids hashids, String userId) {
        if (userId.matches("\\d{1,15}")) {
            return hashids.encode(Long.parseLong(userId));
        }
        return hashids.encodeHex(HexFormat.of().formatHex(userId.getBytes(StandardCharsets.UTF_8)));
    }
}
