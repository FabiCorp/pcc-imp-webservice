package edu.kit.informatik.pcc.service.manager;

import edu.kit.informatik.pcc.service.data.Account;
import edu.kit.informatik.pcc.service.data.DatabaseManager;
import edu.kit.informatik.pcc.service.data.VideoInfo;
import org.apache.commons.validator.routines.EmailValidator;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.logging.Logger;

/**
 * The AccountManager processes requests concerning accounts and account information.
 * It takes requests from the ServerProxy handles them and forwards them to the
 * Model devices.
 *
 * @author David Laubenstein, Fabian Wenzel
 */
public class AccountManager {

    // status message constants
    private final static String SUCCESS = "SUCCESS";
    private final static String FAILURE = "FAILURE";

    /* #############################################################################################
     *                                  attributes
     * ###########################################################################################*/

    /**
     * Active user account.
     */
    private Account account;
    /**
     * Manager for database access.
     */
    private DatabaseManager databaseManager;

    /* #############################################################################################
     *                                  constructors
     * ###########################################################################################*/

    /**
     * Creates a new account manager for the given account.
     *
     * @param account Active user account.
     */
    public AccountManager(Account account) {
        this.account = account;
        databaseManager = new DatabaseManager(account);
    }

    /* #############################################################################################
     *                                  methods
     * ###########################################################################################*/

    /**
     * Changes the active account to a new one.
     *
     * @param newAccountData JSON String of the account to change to.
     * @return Returns status string of the request.
     */
    public String changeAccount(String newAccountData) {
        Account newAccount = new Account();
        if (!newAccount.hashPassword(getSalt()))
            return FAILURE;
        String status = "NOTHING CHANGED";
        if (!newAccount.getMail().equals(account.getMail())) {
            if (!databaseManager.setMail(newAccount.getMail())) {
                return FAILURE;
            } else {
                status = SUCCESS;
            }
        }

        String newPwHash = newAccount.getPasswordHash();
        if (!newPwHash.equals(account.getPasswordHash())) {
            return databaseManager.setPassword(newPwHash) ? SUCCESS : FAILURE;
        }
        return status;
    }

    /**
     * Gets the unique account id of an account.
     *
     * @return Returns the unique account id.
     */
    public int getAccountId() {
        return databaseManager.getAccountId();
    }

    /**
     * Tries to register an account. The UUID gets stored as a value used to
     * enable account verification later.
     *
     * @param uuid Unique id used to verify registration.
     * @return Returns status of the account creation.
     */
    public String registerAccount(String uuid) {
        byte[] salt = createSalt();

        if (salt == null)
            return FAILURE;

        account.hashPassword(salt);
        boolean validMail = isValidMail(account.getMail());
        if (!validMail) {
            return FAILURE;
        }

        String saltString = Base64.getEncoder().encodeToString(salt);
        return databaseManager.register(uuid, saltString) ? SUCCESS : FAILURE;
    }

    /**
     * Deletes an account from the server. Therefore all videos and metadata registered to the account
     * get deleted as well.
     *
     * @param videoManager VideoManager used to delete videos and metadata of an account
     * @return Returns status of the account deletion.
     */
    public String deleteAccount(VideoManager videoManager) {
        ArrayList<VideoInfo> videoInfoList = databaseManager.getVideoInfoList();
        if (videoInfoList != null) {
            for (VideoInfo videoInfo : videoInfoList) {
                String status = videoManager.videoDelete(videoInfo.getVideoId());
                if (status.equals(FAILURE)) {
                    Logger.getGlobal().warning("An error occurred deleting videos!");
                    return FAILURE;
                }
            }
        }
        return databaseManager.deleteAccount() ? SUCCESS : FAILURE;
    }

    /**
     * Tries to authenticate an account via its main and password.
     *
     * @return Returns if authentication worked successfully
     */
    public boolean authenticate() {
        return databaseManager.authenticate();
    }

    /**
     * Verifies an account by comparing the uuid to the stored one.
     *
     * @param uuid string uuid to compare with uuid in database
     * @return string if deletion successfully accomplished
     */
    public String verifyAccount(String uuid) {
        return databaseManager.verifyAccount(uuid);
    }

    /**
     * Checks if the active account is verified or not.
     *
     * @return boolean if account is verified
     */
    public boolean isVerified() {
        return databaseManager.isVerified();
    }

    /* #############################################################################################
     *                                  helper methods
     * ###########################################################################################*/

    /**
     * This method fetches the salt for the given account from the database,
     * which is represented as encoded string. this string will be decoded an
     *
     * @return salt from database returned as byte[]
     */
    public byte[] getSalt() {
        String saltString = databaseManager.getSalt();
        if (saltString == null) {
            return null;
        }
        try {
            return Base64.getDecoder().decode(saltString);
        } catch (IllegalArgumentException e) {
            Logger.getGlobal().warning("byte[] should at least be two bytes for Base64!");
            return null;
        }
    }

    private byte[] createSalt () {
        //Always use a SecureRandom generator
        SecureRandom sr;
        try {
            sr = SecureRandom.getInstance("SHA1PRNG", "SUN");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            Logger.getGlobal().warning("An error occurred getting a secure random instance!");
            return null;
        }
        //Create array for salt
        byte[] salt = new byte[16];
        //Get a random salt
        sr.nextBytes(salt);
        return salt;
    }


    /**
     * Method to check if mail given by client is valid
     *
     * @param email to validate
     * @return      true if mail is valid and false if not
     */
    private boolean isValidMail(String email) {
        if (email == null || "".equals(email))
            return false;

        email = email.trim();

        EmailValidator ev = EmailValidator.getInstance();
        return ev.isValid(email);

    }


}
