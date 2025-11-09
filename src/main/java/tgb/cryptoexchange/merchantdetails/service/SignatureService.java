package tgb.cryptoexchange.merchantdetails.service;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class SignatureService {

    public String hmacSHA1(String data, String secret) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(rawHmac);
    }

    public String hmacSHA256(String requestJson, URI url, String signKey) {
        String signatureString = requestJson + url.getPath() +
                (url.getQuery() != null ? url.getQuery() : "");
        return hmacSHA256(signatureString, signKey);
    }

    public String hmacSHA256(String signatureString, String signKey) {
        HmacUtils hmacUtils = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, signKey.getBytes(StandardCharsets.UTF_8));
        byte[] hmacSha256 = hmacUtils.hmac(signatureString.getBytes(StandardCharsets.UTF_8));
        return Hex.encodeHexString(hmacSha256);
    }

    public String pandaPayHmacSHA256(String data, String secret) throws NoSuchAlgorithmException, InvalidKeyException {
        // Создаем HMAC SHA256 хэш
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKey);

        byte[] hmacBytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        // Конвертируем в HEX строку
        return HexFormat.of().formatHex(hmacBytes);
    }

    public String getMD5Hash(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hashBytes = md.digest(input.getBytes());

        // Конвертируем байты в HEX
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = String.format("%02x", b);
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public String md5WellBit(String data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(data.getBytes());
        BigInteger no = new BigInteger(1, messageDigest);
        StringBuilder hashtext = new StringBuilder(no.toString(16));
        while (hashtext.length() < 32) {
            hashtext.insert(0, "0");
        }
        return hashtext.toString();
    }

    public String generateHmacSha512Signature(String data, String secret)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha512Hmac = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA512");
        sha512Hmac.init(secretKey);
        byte[] hmacBytes = sha512Hmac.doFinal(
                data.getBytes(StandardCharsets.UTF_8));

        // Возвращаем HEX-строку
        return bytesToHex(hmacBytes);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
