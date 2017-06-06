package edu.kit.informatik.pcc.service.data;

import edu.kit.informatik.pcc.service.server.Main;

import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * This class handles all low-level database queries
 *
 * @author David Laubenstein
 */
public class DatabaseManager {

    // database constants
    private static final String PORT = "5432";
    private static final String HOST = "localhost";
    private static final String DB_NAME = "privacycrashcam";
    private static final String USER = "postgres";
    private static final String PASSWORD = "pccdata";

    /* #############################################################################################
     *                                  attributes
     * ###########################################################################################*/

    /**
     * User account of the active user.
     */
    private Account account;
    /**
     * Connection to the postgres database.
     */
    private Connection c = null;

    /* #############################################################################################
     *                                  constructors
     * ###########################################################################################*/

    /**
     * Constructor, which includes the {@link Account} object in the {@link DatabaseManager}.
     * The {@link Account} object saves important information about the user.
     *
     * @param account to have access to the actual account.
     */
    public DatabaseManager(Account account) {
        // create access to account
        this.account = account;
    }

    /* #############################################################################################
     *                                  methods
     * ###########################################################################################*/

    /**
     * This method is used to open a connection to the database with the class attributes.
     * If the connection fails, the
     * <p>
     * IMPORTANT: A Connection c will be opened, but not closed. After calling this method,
     * you have to close the connection with <b>this.c.close()</b>
     * </p>
     */
    private boolean connectDatabase() {
        c = null;
        try {
            Class.forName("org.postgresql.Driver");
            this.c = DriverManager
                    .getConnection("jdbc:postgresql://" + HOST + ":" + PORT + "/" + DB_NAME, USER, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getGlobal().severe("No connection to database!");
            Main.stopServer();
            return false;
        }
        return true;
    }

    /**
     * Save Video and Metadata to database, related to an account
     * This is not the real file or the path to the file. This is only the name of the
     * video file and metadata file as a String
     *
     * @param videoName name of the video file, which should be saved
     * @param metaName  name of the meta data file, which should be saved
     * @return boolean to indicate success or failure
     */
    public boolean saveProcessedVideoAndMeta(String videoName, String metaName) {
        if (!connectDatabase()) return false;
        // send sql command and catch possible exeptions
        try {
            Statement stmt = this.c.createStatement();
            // sql command
            String sql = "insert into \"video\" (user_id,video_name,meta_name) values (" + account.getId() + ",'" +
                    videoName + "','" + metaName + "');";
            stmt.executeUpdate(sql);
            stmt.close();
            this.c.close();
        } catch (NullPointerException | SQLException e) {
            Logger.getGlobal().warning("Inserting video and meta in database failed!");
        }
        return true;
    }

    /**
     * Get the saved information for a video as a {@link VideoInfo} object.
     * The database command selects the video_name and the id of the video from the
     * table <b>video</b> and creates a {@link VideoInfo} object with this information
     *
     * @param videoId is the id of the video the information should be collected
     * @return a VideoInfo object, where all information are saved in the database
     */
    public VideoInfo getVideoInfo(int videoId) {
        VideoInfo vI = null;
        if (!connectDatabase() || videoId == -1) return null;
        // execute sql command and insert result in ArrayList
        try {
            Statement stmt = this.c.createStatement();
            ResultSet rs = stmt.executeQuery("select \"video_name\",vid.\"id\" from \"video\" as vid  " +
                    "join \"user\" as usr ON vid.user_id=usr.id where usr.id='" + account.getId() + "' " +
                    "AND vid.\"id\"=" + videoId);
            // insert result in ArrayList
            while (rs.next()) {
                String video_name = rs.getString("video_name");
                int id = Integer.parseInt(rs.getString("id"));
                vI = new VideoInfo(id, video_name);
            }
            rs.close();
            stmt.close();
            this.c.close();
        } catch (NullPointerException | SQLException e) {
            Logger.getGlobal().warning("Select SQL command has not been executed successfully: ");
        }
        return vI;
    }

    /**
     * Get all videos of a user packed in {@link VideoInfo} objects
     * The SQL command collects all video_name and id's of the videos and store them into an ArrayList of
     * {@link VideoInfo} objects.
     *
     * @return all information for the videos of a user
     */
    public ArrayList<VideoInfo> getVideoInfoList() {
        // create ArrayList
        ArrayList<VideoInfo> videoInfoList = new ArrayList<>();
        // connect to database
        if (!connectDatabase()) return null;
        // execute sql command and insert result in ArrayList
        try {
            Statement stmt = this.c.createStatement();
            ResultSet rs = stmt.executeQuery("select \"video_name\",vid.\"id\" from \"video\" as vid  " +
                    "join \"user\" as usr ON vid.user_id=usr.id where usr.id='" + account.getId() + "'");
            // insert result in ArrayList
            while (rs.next()) {
                String video_name = rs.getString("video_name");
                int id = Integer.parseInt(rs.getString("id"));
                VideoInfo vI = new VideoInfo(id, video_name);
                videoInfoList.add(vI);
            }
            rs.close();
            stmt.close();
            this.c.close();
        } catch (NullPointerException | SQLException e) {
            Logger.getGlobal().warning("SELECT SQL command has not been executed successfully: ");
        }
        return videoInfoList;
    }

    /**
     * Delete the row with all information for the video in the table <b>video</b>
     * But the deletion is just in the database, the files are already existing.
     * <p><b>
     * You have to delete the videos before you call this method,
     * so you are not able to find the video anymore
     * </b></p>
     *
     * @param videoId: the <b>unique</b> id of the video
     * @return a boolean, to review the success of the sql statement
     */
    public boolean deleteVideoAndMeta(int videoId) {
        if (!connectDatabase() || videoId == -1) return false;
        try {
            Statement stmt = this.c.createStatement();
            // sql command
            String sql = "DELETE from \"video\" where id=" + videoId + ";";
            stmt.executeUpdate(sql);
            stmt.close();
            this.c.close();
            return true;
        } catch (NullPointerException | SQLException e) {
            Logger.getGlobal().severe("DELETE SQL command has not been executed successfully: " +
                    "There is a problem with the Video, maybe the id of the video: ");
        }
        return false;
    }

    /**
     * Changes the mail address of an account
     * <p>
     * Check, if the mail is not given to another user, because the column "mail" is unique.
     * If the mail is not assigned to another account, the new mail will be set
     * </p>
     *
     * @param newMail: mail address, which should be updated in the account, if the mail is not assigned to another
     *                 account
     * @return if the mail address was changed
     */
    public boolean setMail(String newMail) {
        // connect to database
        if (!connectDatabase()) return false;
        // send sql command and catch possible exeptions
        try {
            Statement stmt = this.c.createStatement();
            // if mail is not given to another account, execute sql command
            if (!isMailExisting(newMail)) {
                String sql = "UPDATE \"user\" set mail='" + newMail + "' where id=" + account.getId() + ";";
                stmt.executeUpdate(sql);
                stmt.close();
                this.c.close();
                return true;
            } else {
                Logger.getGlobal().warning("The mail address you want to update is already stored" +
                        "in another user account. Please choose another mail address");
                this.c.close();
            }
        } catch (NullPointerException | SQLException e) {
            Logger.getGlobal().warning("Setting the mail in database was not successful");
        }
        return false;
    }

    /**
     * Set new password for user in the database
     *
     * @param newPasswordHash: set this passwordHash as new password for this user
     * @return if setting password was successful
     */
    public boolean setPassword(String newPasswordHash) {
        // connect to database
        if (!connectDatabase()) return false;
        // send sql command and catch possible exeptions
        try {
            Statement stmt = this.c.createStatement();
            // sql command
            String sql = "UPDATE \"user\" set password='" + newPasswordHash + "' where id=" + account.getId() + ";";
            stmt.executeUpdate(sql);
            stmt.close();
            this.c.close();
            return true;
        } catch (NullPointerException | SQLException e) {
            Logger.getGlobal().severe("Failed to set new password");
        }
        return false;
    }

    /**
     * Authenticates user account by checking if mail and password match the entries in the
     * database match the passed ones.
     *
     * @return Whether the entries match each other.
     */
    public boolean authenticate() {
        String mail = "";
        String passwordHash = "";
        // connect to database
        if (!connectDatabase()) return false;
        // execute sql command and insert result in ArrayList
        try {
            Statement stmt = this.c.createStatement();

            ResultSet rs = stmt.executeQuery("select \"mail\",\"password\" from \"user\" where id='" +
                    account.getId() + "'");
            // insert result in ArrayList
            while (rs.next()) {
                mail = rs.getString("mail");
                passwordHash = rs.getString("password");
            }
            rs.close();
            stmt.close();
            this.c.close();
        } catch (NullPointerException | SQLException e) {
            Logger.getGlobal().severe("Error while authenticating account in database");
        }
        //return boolean, if password and mail are equal to database data
        return mail.equals(account.getMail()) && passwordHash.equals(account.getPasswordHash());

    }

    /**
     * Deletes user account.
     *
     * @return Whether deleting was successful or not.
     */
    public boolean deleteAccount() {
        if (!connectDatabase()) return false;
        try {
            Statement stmt = this.c.createStatement();
            // sql command
            String sql = "delete from \"user\" where \"user\".\"id\"='" + account.getId() + "'";
            stmt.executeUpdate(sql);
            stmt.close();
            this.c.close();
        } catch (NullPointerException | SQLException e) {
            Logger.getGlobal().warning("Error while deleting account in database");
        }
        return true;
    }

    /**
     * Get the account id of the active account which is saved in the database.
     *
     * @return account id from the database.
     */
    public int getAccountId() {
        int accountId = -1;
        if (!connectDatabase()) return -1;
        try {
            Statement stmt = this.c.createStatement();
            ResultSet rs = stmt.executeQuery("select \"id\" from \"user\" where \"user\".\"mail\"='" +
                    account.getMail() + "'");
            // insert result in ArrayList
            while (rs.next()) {
                accountId = Integer.parseInt(rs.getString("id"));
            }
            stmt.close();
            this.c.close();
        } catch (SQLException | NullPointerException e) {
            Logger.getGlobal().warning("Retrieving account id from database failed");
            return -1;
        }
        return accountId;
    }

    /**
     * Registers an account.
     * Necessary data is fetched in the account object and the uuid
     *
     * @param uuid is unique and is for verification process
     * @return Returns whether registering was successful or not.
     */
    public boolean register(String uuid, String salt) {
        if (!connectDatabase()) return false;
        // send sql command and catch possible exeptions
        try {
            Statement stmt = this.c.createStatement();
            // sql command
            String sql = "insert into \"user\" (mail,password,uuid,verified,password_salt) values ('" + account
                    .getMail
                            () +
                    "','" +
                    account.getPasswordHash() + "','" + uuid + "', false, '" + salt + "' );";
            stmt.executeUpdate(sql);
            stmt.close();
            this.c.close();
        } catch (NullPointerException | SQLException e) {
            Logger.getGlobal().warning("Account registration has occurred a problem. Check uuid, " +
                    "mail and passwordHash! ");
            return false;
        }
        return true;
    }

    /**
     * Verifies an account, compares uuid of database and uuid of url
     *
     * @param uuid is the uuid, which is in the link of the verification mail
     * @return Returns verification status.
     */
    public String verifyAccount(String uuid) {
        //connect to Database
        if (!connectDatabase()) return "FAILURE";
        // get uuid from account
        boolean uuidVerified = false;
        try {
            Statement stmt = this.c.createStatement();

            ResultSet rs = stmt.executeQuery("select \"verified\" from \"user\" as usr  where usr.uuid='" +
                    uuid + "'");
            // insert result in ArrayList
            if (rs != null && rs.next()) {
                uuidVerified = rs.getBoolean("verified");
            }
            assert rs != null;
            rs.close();
            stmt.close();
        } catch (NullPointerException | SQLException e) {
            Logger.getGlobal().warning("verifyAccount occurred a problem in storing uuid temporarily: ");
            return "FAILURE";
        }
        if (!uuidVerified) {
            try {
                Statement stmt = this.c.createStatement();
                stmt.executeUpdate("update \"user\" set verified=TRUE where uuid='" + uuid + "';");
                stmt.close();
                this.c.close();
                return "SUCCESS";
            } catch (NullPointerException | SQLException e) {
                Logger.getGlobal().severe("A problem occured while updating verification status ");
                return "FAILURE";
            }
        }
        return "ALREADY VERIFIED";
    }

    /**
     * Checks if the verified status of a user is true.
     *
     * @return Returns the verification status of the user.
     */
    public boolean isVerified() {
        // connect to Database
        if (!connectDatabase()) return false;
        boolean verified = false;
        try {
            Statement stmt = this.c.createStatement();

            ResultSet rs = stmt.executeQuery("select \"verified\" from \"user\" where id=" +
                    account.getId() + ";");
            while (rs.next()) {
                verified = rs.getBoolean("verified");
            }
            rs.close();
            stmt.close();
            this.c.close();
        } catch (NullPointerException | SQLException e) {
            Logger.getGlobal().warning("Checking verification status wasn't successful"
                    + e);
        }
        return verified;
    }

    /**
     * Gets the video id of a video by using the video name to find it.
     * Only used for test purposes.
     *
     * @param videoName Video name to look id up for.
     * @return Returns the unique id of the video.
     */
    @Deprecated
    public int getVideoIdByName(String videoName) {
        if (!connectDatabase()) return -1;
        int id = -1;
        try {
            Statement stmt = this.c.createStatement();

            ResultSet rs = stmt.executeQuery("select id from \"video\" where \"video_name\"='" +
                    videoName + "'");
            // insert result in ArrayList
            while (rs.next()) {
                id = Integer.parseInt(rs.getString("id"));
            }
            rs.close();
            stmt.close();
            this.c.close();
        } catch (NullPointerException | SQLException e) {
            Logger.getGlobal().warning("Retrieving vide id from database failed.");
        }
        return id;
    }

    /**
     * Get the name of the metadata file, which will be searched by the id of the video
     *
     * @param videoId: to get the related video to the metadata
     * @return String of metadata name
     */
    public String getMetaName(int videoId) {
        //connect to database
        if (!connectDatabase())
            return null;

        try {
            Statement stmt = this.c.createStatement();

            ResultSet rs = stmt.executeQuery("select \"meta_name\" from \"video\" as vid where vid.id=" +
                    videoId + ";");
            // insert result in ArrayList
            if (rs.next()) {
                return rs.getString("meta_name");
            }
            rs.close();
            stmt.close();
            this.c.close();
        } catch (NullPointerException | SQLException e) {
            Logger.getGlobal().severe("Retrieving metadata from database failed");
        }
        try {
            this.c.close();
        } catch (SQLException e) {
            Logger.getGlobal().warning("connection to database not closed!");
        }
        return null;
    }

    /**
     * return the salt of the hashed password. uses the account.getId() of the account object.
     * will be converted to String when saved to database
     *
     * @return the salt of the hashed password, which is normally a byte[] array.
     */
    public String getSalt() {
        //connect to database
        if (!connectDatabase()) return null;
        String salt = null;
        try {
            Statement stmt = this.c.createStatement();

            ResultSet rs = stmt.executeQuery("select \"password_salt\" from \"user\" where \"user\".\"id\"=" +
                    account.getId() + ";");
            // insert result in ArrayList
            if (rs.next()) {
                salt = rs.getString("password_salt");
            }
            rs.close();
            stmt.close();
            this.c.close();
        } catch (NullPointerException | SQLException e) {
            Logger.getGlobal().severe("Retrieving metadata from database failed");
        }
        return salt;
    }

    /**
     * check, if a mail address is already saved in database table <b>user</b>
     *
     * @param mail is the mail, which will be checked
     * @return if mail exists in database
     */
    public boolean isMailExisting(String mail) {
        //connect to database
        if (!connectDatabase()) return false;
        int count_mail = 0;
        try {
            Statement stmt = this.c.createStatement();

            ResultSet rs = stmt.executeQuery("select count(mail) from \"user\" where mail='" + mail + "';");
            // insert result in ArrayList
            if (rs.next()) {
                count_mail = rs.getInt("count");
            }
            rs.close();
            stmt.close();
            this.c.close();
        } catch (NullPointerException | SQLException e) {
            Logger.getGlobal().warning("Checking for mail existence in database failed");
        }
        return count_mail != 0;
    }

    /* #############################################################################################
    *                                  useful methods
    * ###########################################################################################*/

    /**
     * <p>check if video is uploaded and matching to user</p>
     *
     * @param videoId to check, if videoId matches to the valid user
     * @return if video matches to user
     */
    public boolean videoId_UserId_Matching(int videoId) {
        try {
            if (!connectDatabase()) return false;
            Statement stmt = this.c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT user_id FROM \"video\" WHERE id=" + videoId + "");
            // insert result in ArrayList
            if (rs.next()) {
                int userIdDatabase = rs.getInt("user_id");
                if (userIdDatabase == account.getId()) {
                    return true;
                }
            }
            rs.close();
            stmt.close();
            this.c.close();
        } catch (SQLException e) {
            Logger.getGlobal().warning("SQL Exception in videoId_UserId_Matching(). " +
                    "Maybe the video is not matching to your account!");
        }
        return false;
    }
}