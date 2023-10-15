package com.stc.inspireu.utils;

import org.apache.commons.io.FilenameUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

public class Encryption {


    private static final String ALGO = "AES";
    private static final byte[] keyValue = new byte[] { 'T', 'h', 'e', 'B', 'e', 's', 't', 'S', 'e', 'c', 'r', 'e', 't',
        'K', 'e', 'y' };

    public static String encrypt(String Data) {
        try {
            Key key = generateKey();
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.ENCRYPT_MODE, key);
            byte[] encVal = c.doFinal(Data.getBytes());
            String encryptedValue = Base64.getEncoder().encodeToString(encVal);
            if (encryptedValue.contains("/"))
                encryptedValue = encryptedValue.replace("/", "___");
            return encryptedValue;
        } catch (Exception e) {
            return null;
        }
    }

    public static String decrypt(String encryptedData) {
        try {
            if (encryptedData != null && !encryptedData.isEmpty()) {
                encryptedData = encryptedData.replace(" ", "+");
                if (encryptedData.contains("___"))
                    encryptedData = encryptedData.replace("___", "/");
                Key key = generateKey();
                Cipher c = Cipher.getInstance(ALGO);
                c.init(Cipher.DECRYPT_MODE, key);
                byte[] decordedValue = Base64.getDecoder().decode(encryptedData);
                byte[] decValue = c.doFinal(decordedValue);
                String decryptedValue = new String(decValue);
                return decryptedValue;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public static Long decryptId(String encryptedData) {
        Long result = null;
        String decryptedValue = decrypt(encryptedData);
        try {
            result = new Long(decryptedValue);
        } catch (Exception e) {

        }
        return result;
    }

    public static String encryptId(Long id) {
        String result = null;
        try {
            result = encrypt(id.toString());
        } catch (Exception e) {

        }
        return result;
    }

    private static Key generateKey() throws Exception {
        Key key = new SecretKeySpec(keyValue, ALGO);
        return key;
    }

    public static String getRandomWord(int length) {
        String r = "";
        for (int i = 0; i < length; i++) {
            r += (char) (Math.random() * 26 + 97);
        }
        return r.toUpperCase();
    }

}
