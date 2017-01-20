package edu.kit.informatik.pcc.service.data;

import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Logger;

public class DatabaseManager { //TODO: LOGGER
	// attributes
	private Account account;
	private static String PORT = "5432";
	private static String HOST = "localhost";
	private static String DB_NAME = "PrivacyCrashCam";
	private Connection c = null;
	// constructors
	public void DatabaseManager(Account account) {
	    // create access to account
	    this.account = account;
	}
	// methods
	public void connectDatabase() {
		c = null;
		try {
			Class.forName("org.postgresql.Driver");
         	this.c = DriverManager
            	.getConnection("jdbc:postgresql://" + HOST + ":" + PORT + "/" + DB_NAME + "");
      	} catch (Exception e) {
			e.printStackTrace();
        	System.err.println(e.getClass().getName()+": "+e.getMessage());
	        System.exit(0);
      	}
	}

	public boolean saveProcessedVideoAndMeta(String videoName, String metaName) {
        connectDatabase();
		// send sql command and catch possible exeptions
        try {
            Statement stmt = null;
            stmt = this.c.createStatement();
            // sql command
			//TODO: change id in sql command
			String sql = "insert into \"video\" (user_id,video_name,meta_name) values (" + account.getId() + ",'" + videoName + "','" + metaName + "');";
            stmt.executeUpdate(sql);
            this.c.commit();
            stmt.close();
            this.c.close();
        } catch (NullPointerException nPE) {
        	Logger.getGlobal().severe("saveProcessedVideoAndMeta " + nPE);
        } catch (SQLException sqlException) {
            Logger.getGlobal().severe("saveProcessedVideoAndMeta " + sqlException);
        }
		return true;
	}

	public VideoInfo getVideoInfo(int videoId) {
        //TODO: write method

        VideoInfo vI = null;
		// execute sql command and insert result in ArrayList
		try {
			Statement stmt = null;
			stmt = this.c.createStatement();
			ResultSet rs = stmt.executeQuery( "select \"video_name\",vid.\"id\" from \"video\" as vid  join \"user\" as usr ON vid.user_id=usr.id where usr.id='" + account.getId() + "' AND vid.\"id\"=" + videoId);
			// insert result in ArrayList
			while ( rs.next() ) {
				String video_name = rs.getString("video_name");
				int id = Integer.parseInt(rs.getString("id"));
				vI = new VideoInfo(id, video_name);
			}
			rs.close();
			stmt.close();
			this.c.close();
		} catch (NullPointerException nPE) {
			Logger.getGlobal().severe("getVideoInfoList " + nPE);
		} catch (SQLException sqlException) {
			Logger.getGlobal().severe("getVideoInfoList " + sqlException);
		}
		return vI;
	}

	/**
	 * return an ArrayList, which includes all Videos of a user
	 * @return ArrayList ov VideoInfo-Objects
	 */
	public ArrayList<VideoInfo> getVideoInfoList() {
		// create ArrayList
		ArrayList<VideoInfo> videoInfoList= new ArrayList<VideoInfo>();
	    // connect to database
	    connectDatabase();
	    // execute sql command and insert result in ArrayList
	    try {
			Statement stmt = null;
			stmt = this.c.createStatement();
			ResultSet rs = stmt.executeQuery( "select \"video_name\",vid.\"id\" from \"video\" as vid  join \"user\" as usr ON vid.user_id=usr.id where usr.id='" + account.getId() + "'" );
			// insert result in ArrayList
			while ( rs.next() ) {
				String video_name = rs.getString("video_name");
				int id = Integer.parseInt(rs.getString("id"));
				VideoInfo vI = new VideoInfo(id, video_name);
				videoInfoList.add(vI);
			}
			rs.close();
			stmt.close();
			this.c.close();
		} catch (NullPointerException nPE) {
	        Logger.getGlobal().severe("getVideoInfoList " + nPE);
		} catch (SQLException sqlException) {
			Logger.getGlobal().severe("getVideoInfoList " + sqlException);
		}
		return videoInfoList;
	}

	/**
	 * delete the row with all infos for the video, but just in Database, the files are already existing...
	 * @param videoId: the unique id of the video
	 * @return a boolean, to review the success of the sql statement
	 */
	public boolean deleteVideoAndMeta(int videoId) {
	    connectDatabase();
	    try {
            Statement stmt = null;
            stmt = this.c.createStatement();
            // sql command
            String sql = "DELETE from \"video\" where id=" + videoId + ";";
            stmt.executeUpdate(sql);
            this.c.commit();
            stmt.close();
            this.c.close();
        } catch (NullPointerException nPE) {
	        Logger.getGlobal().severe("deleteVideoAndMeta " + nPE);
        } catch (SQLException sqlException) {
			Logger.getGlobal().severe("deleteVideoAndMeta " + sqlException);
        }
		return false;
	}

	/**
	 * return metadata of a videoId
	 * @param videoId: unique id of a video
	 * @return a metadata-object
	 */
	public Metadata getMetaData(int videoId){
	    // create String, where meta file is stored
	    String filePath = LocationConfig.META_DIR + "/" + getMetaNameByVideoId(videoId);
	    // read Meta file to get infos
        // TODO read Meta file to get infos
	    // Metadata meta = new Metadata();
        // return meta;
        return null;
	}

	/**
	 * changes the mail address of an account
	 * @param newMail: new Mail address which will be set as new mail address
	 * @return boolean, which symbolizes the success of the sql statement
	 */
	public boolean setMail(String newMail) {
	    // connect to database
        connectDatabase();
        // send sql command and catch possible exeptions
        try {
            Statement stmt = null;
            stmt = this.c.createStatement();
            // sql command
            String sql = "UPDATE \"user\" set mail='" + newMail + "' where id=" + account.getId() + ";";
            stmt.executeUpdate(sql);
            this.c.commit();
            stmt.close();
            this.c.close();
        } catch (NullPointerException nPE) {
            Logger.getGlobal().severe("setMail " + nPE);
        } catch (SQLException sqlException) {
        	Logger.getGlobal().severe("setMail " + sqlException);
        }
        return true;
	}

	/**
	 * set new password for user
	 * @param newPasswordHash: set this passwordHash as new password for this user
	 * @return boolean if sql statement was successful
	 */
	public boolean setPassword(String newPasswordHash) {
        // connect to database
        connectDatabase();
        // send sql command and catch possible exeptions
        try {
            Statement stmt = null;
            stmt = this.c.createStatement();
            stmt = c.createStatement();
            // sql command
            String sql = "UPDATE \"user\" set password='" + newPasswordHash + "' where id=" + account.getId() + ";";
            stmt.executeUpdate(sql);
            this.c.commit();
            stmt.close();
            this.c.close();
        } catch (NullPointerException nPE) {
            Logger.getGlobal().severe("setPassword " + nPE);
        } catch (SQLException sqlException) {
            Logger.getGlobal().severe("setPassword " + sqlException);
        }
		return false;
	}

	/**
	 * authenticate account (check, if password and mail are correct)
	 * @return boolean true=successful auth, false=wrong passwd or mail
	 */
	public boolean authenticate() {
	    String mail = "";
	    String passwordHash = "";
	    // connect to database
	    connectDatabase();
	    // execute sql command and insert result in ArrayList
	    try {
			Statement stmt = null;
			stmt = this.c.createStatement();

			ResultSet rs = stmt.executeQuery( "select \"mail\",\"password\" from \"user\" where id='" + account.getId() + "'" );
			// insert result in ArrayList
			while ( rs.next() ) {
				mail = rs.getString("mail");
				passwordHash = rs.getString("password");
			}
			rs.close();
			stmt.close();
			this.c.close();
		} catch (NullPointerException nPE) {
	        Logger.getGlobal().severe("authenticate " + nPE);
		} catch (SQLException sqlException) {
	        Logger.getGlobal().severe("authenticate " + sqlException);
		}
		//return boolean, if password and mail are equal to database data
		return mail.equals(account.getEmail()) && passwordHash.equals(account.getPasswordHash());

	}

	/**
	 * delete user row
	 * @return boolean that symbolizes success of deletion
	 */
	public boolean deleteAccount() {
		connectDatabase();
		try {
			Statement stmt = null;
			stmt = this.c.createStatement();
			// sql command
            String sql = "delete from \"user\" where \"user\".\"id\"=" + account.getId();
			stmt.executeUpdate(sql);
			this.c.commit();
			stmt.close();
			this.c.close();
		} catch (NullPointerException nPE) {
		    Logger.getGlobal().severe("deleteAccount " + nPE);
		} catch (SQLException sqlException) {
		    Logger.getGlobal().severe("deleteAccount " + sqlException);
		}
		return true;
	}

	/**
	 * get the account id, which is saved in the database
	 * @return int of account id, which is the database
	 */
	public int getAccountId() {
	    int accountId = -2;
	    connectDatabase();
		try {
			Statement stmt = null;
			stmt = this.c.createStatement();
			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery( "select \"id\" from \"user\" where \"user\".\"mail\"=" + account
					.getEmail());
			// insert result in ArrayList
			while ( rs.next() ) {
			   accountId = Integer.parseInt(rs.getString("id"));
			}
			this.c.commit();
			stmt.close();
			this.c.close();
		} catch (SQLException sqlException) {
			System.out.println(sqlException);
			return -1;
		}
		return accountId;
	}

	/**
	 * register an account. necessary data is the account object and the uuid
	 * @param uuid is unique and is for verification process
	 * @return boolean, which symbolizes success of sql statement
	 */
	public boolean register(String uuid) {
		connectDatabase();
		//TODO: check, if mail is already existing. If not, create Account
		// send sql command and catch possible exeptions
        try {
            Statement stmt = null;
            stmt = this.c.createStatement();
            // sql command
            //TODO: change id
			String sql = "insert into \"user\" (mail,password,uuid,verified) values ('" + account.getEmail() + "','" + account.getPasswordHash() + "'," + uuid + ",false);";
            stmt.executeUpdate(sql);
            this.c.commit();
            stmt.close();
            this.c.close();
        } catch (NullPointerException nPE) {
            Logger.getGlobal().severe("register " + nPE);
            return false;
        } catch (SQLException sqlException) {
            Logger.getGlobal().severe("register " + sqlException);
            return false;
        }
		return true;
	}

	/**
	 * verifies an account, compare uuid of database and uuid of url
	 * @param uuid is the uuid, which is in the link of the verification mail
	 * @return String of success
	 */
	public boolean verifyAccount(String uuid) {
	    String ret = "";
        //connect to Database
		connectDatabase();
		// get uuid from account
        String uuidDatabase = "";
		try {
			Statement stmt = null;
			stmt = this.c.createStatement();

			ResultSet rs = stmt.executeQuery( "select \"uuid\" from \"user\" as usr  where usr.id='" + account.getId() + "'" );
			// insert result in ArrayList
			uuidDatabase= rs.getString("uuid");
			rs.close();
			stmt.close();
		} catch (NullPointerException nPE) {
		    Logger.getGlobal().severe("verifyAccount " + nPE);
		} catch (SQLException sqlException) {
		    Logger.getGlobal().severe("verifyAccount " + sqlException);
		}
		if (uuidDatabase.equals(uuid)) {
            try {
                Statement stmt = null;
                stmt = this.c.createStatement();

                stmt.executeQuery("update \"user\" set verified=TRUE where id=" + account.getId() + ";");
                stmt.close();
                this.c.close();
            } catch (NullPointerException nPE) {
                System.out.println(nPE);
            } catch (SQLException sqlException) {
                System.out.println(sqlException);
            }
            return true;
		} else {
		    Logger.getGlobal().severe("verifyAccount: uuid not like uuid in database");
            // close c, because when if=true, c is needed
            try {
                this.c.close();
            } catch (SQLException sqlE) {
                System.out.println(sqlE);
            }
            return false;
        }
	}

	/**
	 * check, if the value "verified" in table "user" is true or false
	 * @return value "verified" in table "user"
	 */
	public boolean isVerified() {
	    // connect to Database
	    connectDatabase();
	    int verified = 2;
        try {
            Statement stmt = null;
            stmt = this.c.createStatement();

            ResultSet rs = stmt.executeQuery("select \"verified\" from \"user\" where id=" + account.getId() + ";");
            // insert result in ArrayList
            if (rs.getFetchSize() <= 1) {
                verified = Integer.parseInt(rs.getString("meta_name"));
            }
            rs.close();
            stmt.close();
            this.c.close();
        } catch (NullPointerException nPE) {
            Logger.getGlobal().severe("isVerified: " + nPE);
        } catch (SQLException sqlException) {
            Logger.getGlobal().severe("isVerified " + sqlException);
        }
        if(verified == 0) {
            return false;
        } else if (verified == 1) {
            return true;
        } else {
            Logger.getGlobal().severe("Hard problem in isVerified!!!");
            return false;
        }
	}

	// getter/setter

	/**
	 * get the name of the metadata file
	 * @param videoId: to get the related video to the metadata
	 * @return String of metadata name
	 */
    private String getMetaNameByVideoId(int videoId) {
	    //connect to database
	    connectDatabase();
        String meta = "";
	    try {
			Statement stmt = null;
			stmt = this.c.createStatement();

			ResultSet rs = stmt.executeQuery("select \"meta_name \" from \"video \" as vid where vid.id=" + videoId + ";");
			// insert result in ArrayList
            if (rs.getFetchSize() <= 1) {
                meta = rs.getString("meta_name");
            }
			rs.close();
			stmt.close();
			this.c.close();
		} catch (NullPointerException nPE) {
			System.out.println(nPE);
		} catch (SQLException sqlException) {
			System.out.println(sqlException);
        }
		return meta;
    }
}
