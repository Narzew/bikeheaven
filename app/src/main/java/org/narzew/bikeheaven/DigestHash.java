package org.narzew.bikeheaven;

import android.content.Context;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Class containing functions to hash text using SHA1
 */

public class DigestHash {

    /**
     * Class context
     */
    protected Context context;
    /**
     * Variable containing key to logcat tool
     */
    private String LOG_KEY = Config.LOG_KEY;
    /**
     * Create class instance with given context
     *
     * @param context Context
     */
    public DigestHash(Context context) {
        this.context = context;
    }

    /**
     * Converts byte array to hexadecimal String
     *
     * @param data	Byte array to be converted
     * @return Hexadecimal string
     */

    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    /**
     * Hash String using SHA1-Algorithm
     *
     * @param text	Text to hash
     * @return	Hashed text
     */

    public static String sha1(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(text.getBytes("utf-8"), 0, text.length());
            byte[] sha1hash = md.digest();
            return convertToHex(sha1hash);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

}
