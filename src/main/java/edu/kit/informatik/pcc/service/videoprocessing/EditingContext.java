package edu.kit.informatik.pcc.service.videoprocessing;

import edu.kit.informatik.pcc.service.data.Account;
import edu.kit.informatik.pcc.service.data.LocationConfig;
import edu.kit.informatik.pcc.service.data.Metadata;
import edu.kit.informatik.pcc.service.data.VideoInfo;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Data container for all data needed by the video processing chain's stages.
 *
 * @author Josh Romanowski
 */
public class EditingContext {

    /* #############################################################################################
     *                                  attributes
     * ###########################################################################################*/

    /**
     * Location of the encrypted video.
     */
    private File encVid;
    /**
     * Location of the encrypted symmetric key.
     */
    private File encKey;
    /**
     * Location of the encrypted metadata.
     */
    private File encMetadata;
    /**
     * Location of the decrypted video.
     */
    private File decVid;
    /**
     * Location of the decrypted metadata.
     */
    private File decMetadata;
    /**
     * Location of the anonymized video.
     */
    private File anonymizedVid;
    /**
     * Location of the anonymized video after adding metadata.
     */
    private File vidWithMeta;
    /**
     * User account of the processed video.
     */
    private Account account;
    /**
     * Video name of the processed video.
     */
    private String videoName;

    /* #############################################################################################
     *                                  constructors
     * ###########################################################################################*/

    /**
     * Creates all necessary data for processing the video
     * out of the user's account and the video name.
     *
     * @param account   User account of the user that uploaded the video.
     * @param videoName Video name of the uploaded video.
     */
    public EditingContext(Account account, String videoName) {

        this.account = account;
        this.videoName = videoName;

        this.encVid = new File(LocationConfig.TEMP_DIR
                + File.separator + account.getId() + "_" + videoName + "_" + "encVid" + VideoInfo.FILE_EXTENTION);
        this.encKey = new File(LocationConfig.TEMP_DIR
                + File.separator + account.getId() + "_" + videoName + "_" + "encKey.txt");
        this.encMetadata = new File(LocationConfig.TEMP_DIR
                + File.separator + account.getId() + "_" + videoName + "_" + "encMetadata" + Metadata.FILE_EXTENTION);
        this.decVid = new File(LocationConfig.TEMP_DIR
                + File.separator + account.getId() + "_" + videoName + "_" + "decVid" + VideoInfo.FILE_EXTENTION);
        this.decMetadata = new File(LocationConfig.TEMP_DIR
                + File.separator + account.getId() + "_" + videoName + "_" + "meta" + Metadata.FILE_EXTENTION);
        this.anonymizedVid = new File(LocationConfig.TEMP_DIR
                + File.separator + account.getId() + "_" + videoName + "_" + "anonym" + VideoInfo.FILE_EXTENTION);
        this.vidWithMeta = new File(LocationConfig.TEMP_DIR
                + File.separator + account.getId() + "_" + videoName + "_" + "videoWithMeta" + VideoInfo.FILE_EXTENTION);

    }

    /* #############################################################################################
     *                                  getter/setter
     * ###########################################################################################*/

    public File getEncVid() {
        return encVid;
    }

    public File getEncKey() {
        return encKey;
    }

    public File getEncMetadata() {
        return encMetadata;
    }

    public File getDecVid() {
        return decVid;
    }

    public File getDecMetadata() {
        return decMetadata;
    }

    public File getAnonymizedVid() {
        return anonymizedVid;
    }

    public Account getAccount() {
        return account;
    }

    public String getVideoName() {
        return videoName;
    }

    public List<File> getAllTempFiles() {
        LinkedList<File> files = new LinkedList<>();
        files.add(encVid);
        files.add(encMetadata);
        files.add(encKey);
        files.add(decVid);
        files.add(decMetadata);
        files.add(anonymizedVid);
        files.add(vidWithMeta);
        return files;
    }
}
