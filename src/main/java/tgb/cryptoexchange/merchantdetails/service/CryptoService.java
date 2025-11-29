package tgb.cryptoexchange.merchantdetails.service;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.util.Base64;

@Service
public class CryptoService {

    public String decrypt(String keyB64, String tokenB64) throws GeneralSecurityException {
        byte[] key = Base64.getDecoder().decode(keyB64);
        byte[] data = Base64.getUrlDecoder().decode(tokenB64);

        byte[] iv = new byte[12];
        System.arraycopy(data, 9, iv, 0, 12);

        byte[] cipherText = new byte[data.length - 9 - 12];
        System.arraycopy(data, 9 + 12, cipherText, 0, cipherText.length);

        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec ks = new SecretKeySpec(key, 0, 16, "AES");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);

        c.init(Cipher.DECRYPT_MODE, ks, spec);
        byte[] out = c.doFinal(cipherText);

        return new String(out);
    }
}
