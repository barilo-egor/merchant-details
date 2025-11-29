package tgb.cryptoexchange.merchantdetails.service;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.util.Base64;

@Service
public class CryptoService {

    public String decrypt(String keyB64, String tokenB64) throws GeneralSecurityException {
        byte[] key = Base64.getDecoder().decode(keyB64);
        byte[] token = Base64.getUrlDecoder().decode(tokenB64);

        if (token.length < 9 + 16 + 32) throw new RuntimeException("Invalid token");

        byte[] aesKey = new byte[16];
        byte[] hmacKey = new byte[16];
        System.arraycopy(key, 0, hmacKey, 0, 16);
        System.arraycopy(key, 16, aesKey, 0, 16);

        byte[] iv = new byte[16];
        System.arraycopy(token, 9, iv, 0, 16);

        int cipherLen = token.length - 9 - 16 - 32;
        byte[] cipherText = new byte[cipherLen];
        System.arraycopy(token, 25, cipherText, 0, cipherLen);

        byte[] hmacFromToken = new byte[32];
        System.arraycopy(token, token.length - 32, hmacFromToken, 0, 32);

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(hmacKey, "HmacSHA256"));
        mac.update(token, 0, token.length - 32);
        byte[] calcHmac = mac.doFinal();

        for (int i = 0; i < 32; i++) if (calcHmac[i] != hmacFromToken[i]) throw new RuntimeException("HMAC mismatch");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new IvParameterSpec(iv));
        return new String(cipher.doFinal(cipherText));
    }
}
