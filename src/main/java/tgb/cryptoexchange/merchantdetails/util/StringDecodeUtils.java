package tgb.cryptoexchange.merchantdetails.util;

public final class StringDecodeUtils {

    private StringDecodeUtils() {
    }

    public static String decodeUnicode(String input) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < input.length()) {
            if (input.charAt(i) == '\\' && i + 5 < input.length() && input.charAt(i + 1) == 'u') {
                String hex = input.substring(i + 2, i + 6);
                sb.append((char) Integer.parseInt(hex, 16));
                i += 6;
            } else {
                sb.append(input.charAt(i));
                i++;
            }
        }
        return sb.toString();
    }
}
