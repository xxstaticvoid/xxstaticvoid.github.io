package com.boardsaver;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {

    private static final int DEPTH = 8; //depth of hash - higher is more secure but slower


    /**
     * Hashes a password using the OpenBSD bcrypt scheme
     *
     * @param password Plaintext password wrote by the user (<72 bytes)
     * @return Outcome of BCrypt hashing function
     * @throws RuntimeException There was an error hashing the password
     */
    public static String hashPassword(String password) throws RuntimeException {
        try {
            String salt = BCrypt.gensalt(DEPTH);
            return BCrypt.hashpw(password, salt);
        } catch (Exception e) {
            throw new RuntimeException("Error trying to hash password");
        }
    }


    /**
     * Checks user's plaintext password against the hashed password
     *
     * @param password Plaintext password wrote by the user that is to be verified
     * @param hashedPassword the previously hashed password
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        try {
            return BCrypt.checkpw(password, hashedPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
