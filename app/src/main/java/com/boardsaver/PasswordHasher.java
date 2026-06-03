package com.boardsaver;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class PasswordHasher {

    public static String hashPassword(String password) throws RuntimeException {
        try {
            //FIXME :: change to BCrypt
            MessageDigest digest = MessageDigest.getInstance("SHA-256"); //low collision hash
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            //convert bytes in hash to hex
            char[] hashAsHex = new char[hash.length * 2]; //allocate for twice the size
            for(int i = 0; i < hash.length; i++) {
                int v = hash[i] & 0xff; //convert to unsigned
                hashAsHex[i * 2] = "0123456789ABCDEF".charAt(v >>> 4); //left side of byte
                hashAsHex[i * 2 + 1] = "0123456789ABCDEF".charAt(v & 0x0F); //right side of byte
            }

            return new String(hashAsHex);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
