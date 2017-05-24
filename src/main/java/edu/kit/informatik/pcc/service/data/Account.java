package edu.kit.informatik.pcc.service.data;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Datacontainer for information concerning user accounts.
 *
 * @author Fabian Wenzel, David Laubenstein, Josh Romanowski
 */
public class Account {

    // JSON keys
    private static final String JSON_KEY_MAIL = "mail";
    private static final String JSON_KEY_PASSWORD = "password";

    /* #############################################################################################
     *                                  attributes
     * ###########################################################################################*/

    /**
     * E-mail address of the account
     */
    private String mail;

    /**
     * Password of the account.
     */
    private String passwordHash;

    /**
     * Clear text password, not accessible.
     */
    private String password;

    /**
     * Unique identifier for each account, maps to the one in the database.
     */
    private int id;


    /* #############################################################################################
     *                                  constructors
     * ###########################################################################################*/

    /**
     * Takes the json string and parses the attributes from it.
     *
     * @param json JSON string which includes account information.
     */
    public Account(String json) {
        JSONObject account = new JSONObject(json);
        this.mail = account.getString(JSON_KEY_MAIL);
        this.password = account.getString(JSON_KEY_PASSWORD);
    }

    /**
     * Hashes the password stored when creating the Account
     *
     * @param salt Salt used for hashing.
     * @return Returns whether hashing the password was successful or not.
     */
    public boolean hashPassword(byte[] salt) {
        if (salt == null) {
            return false;
        }
        try {
            // Create MessageDigest instance for SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            //Add password bytes to digest
            md.update(salt);
            //Get the hash's bytes
            byte[] bytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            //This bytes[] has bytes in decimal format;
            //Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            passwordHash = sb.toString();
            return true;
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
    }


    /* #############################################################################################
     *                                  getter/setter
     * ###########################################################################################*/

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getMail() {
        return mail;
    }
}
