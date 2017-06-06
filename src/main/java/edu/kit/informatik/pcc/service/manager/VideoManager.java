package edu.kit.informatik.pcc.service.manager;

import edu.kit.informatik.pcc.service.data.*;
import edu.kit.informatik.pcc.service.videoprocessing.VideoProcessingChain;
import edu.kit.informatik.pcc.service.videoprocessing.VideoProcessingManager;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.container.AsyncResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * The VideoManager processes requests concerning video up-/download and progression.
 *
 * @author Fabian Wenzel, David Laubenstein
 */
public class VideoManager {

    // processing status constants
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
     * Database manager used to process database queries.
     */
    private DatabaseManager databaseManager;

    /* #############################################################################################
     *                                  constructors
     * ###########################################################################################*/

    /**
     * Creates a new video manager for the given account.
     *
     * @param account Active user account.
     */
    public VideoManager(Account account) {
        this.account = account;
        databaseManager = new DatabaseManager(account);
    }

    /* #############################################################################################
     *                                  methods
     * ###########################################################################################*/

    /**
     * Gets the video information for all videos of a user and creates a JSON string out of it.
     *
     * @return Returns JSON string with a JSON array containing all VideoInfo JSONs.
     */
    public String getVideoInfoList() {
        //convert VideoInfos to JSONArray
        ArrayList<VideoInfo> videoInfoList = databaseManager.getVideoInfoList();
        if (videoInfoList == null) {
            Logger.getGlobal().warning("An error occurred fetching the videoInfoList!");
            return FAILURE;
        }
        JSONArray videoInfoArray = new JSONArray();
        for (int i = 0; i < videoInfoList.size(); i++) {
            String json = videoInfoList.get(i).getAsJson();
            JSONObject jsonObject = new JSONObject(json);
            videoInfoArray.put(i, jsonObject);
        }
        return videoInfoArray.toString();
    }

    /**
     * Uploads a video to the server. Files get uploaded as input streams.
     * As the processing of the video is asynchronous to the uploading, there will not be
     * a useful immediate response.
     *
     * @param video                 inputstream of video file to upload
     * @param metadata              inputstream of metadata file to upload
     * @param encryptedSymmetricKey inputstream of key file to upload
     * @param videoName             name of the uploaded video without extension
     * @param response              asynchronous response used to give response to the client
     */
    public void upload(InputStream video, InputStream metadata, InputStream encryptedSymmetricKey,
                         String videoName, AsyncResponse response) {
        VideoProcessingManager videoProcessingManager = VideoProcessingManager.getInstance();
        videoProcessingManager.addPersistingTask(video, metadata, encryptedSymmetricKey, account, videoName, response);
    }

    /**
     * Uploads a decrypted video to the server. Files get uploaded as input streams.
     * As the processing of the video is asynchronous to the uploading, there will not be
     * a useful immediate response.
     *
     * @param video                 inputstream of video file to upload
     * @param metadata              inputstream of metadata file to upload
     * @param encryptedSymmetricKey inputstream of key file to upload
     * @param videoName             name of the uploaded video without extension
     * @param response              asynchronous response used to give response to the client
     */
    public void uploadDecVid(InputStream video, InputStream metadata, InputStream encryptedSymmetricKey,
                       String videoName, AsyncResponse response) {
        VideoProcessingManager videoProcessingManager = VideoProcessingManager.getInstance();
        videoProcessingManager.addTask(video, metadata, encryptedSymmetricKey, account, videoName, response);
    }

    /**
     * Fetches a video from the database and provides an InputStream to download the video.
     *
     * @param videoId Unique video identifier of the video to download.
     * @return Returns an InputStream for the download.
     */
    public InputStream download(int videoId) {
        if (!videoIdVerification(videoId)) {
            return null;
        }

        VideoInfo videoInfo = databaseManager.getVideoInfo(videoId);

        if (videoInfo == null) {
            return null;
        }

        File video = new File(LocationConfig.ANONYM_VID_DIR + File.separator + account.getId() + "_" +
                videoInfo.getName() + VideoInfo.FILE_EXTENTION);

        InputStream inputStream;
        try {
            inputStream = new FileInputStream(video.getPath());
        } catch (FileNotFoundException e) {
            Logger.getGlobal().warning("An error has occurred finding file " + video.getPath());
            return null;
        }
        return inputStream;
    }

    /**
     * Deletes a video and the respective metadata file from the server.
     *
     * @param videoId of video to delete
     * @return string if file deletion successfully accomplished
     */
    public String videoDelete(int videoId) {
        if (!videoIdVerification(videoId)) {
            return FAILURE;
        }
        VideoInfo videoInfo = databaseManager.getVideoInfo(videoId);
        boolean checkDelete1 = false;
        boolean checkDelete2 = false;
        if (videoInfo == null) {
            return FAILURE;
        }

        // delete video file
        File videoFile = new File(LocationConfig.ANONYM_VID_DIR + File.separator + account.getId() + "_" +
                videoInfo.getName() + VideoInfo.FILE_EXTENTION);
        if (videoFile.exists())
            checkDelete1 = videoFile.delete();

        //delete metadata file.
        String metaName = databaseManager.getMetaName(videoId);
        File metaFile = new File(LocationConfig.META_DIR + File.separator + account.getId() + "_" +
                metaName + Metadata.FILE_EXTENTION);
        if (metaFile.exists())
            checkDelete2 = metaFile.delete();

        //delete both in database
        if (checkDelete1 && checkDelete2) {
            return databaseManager.deleteVideoAndMeta(videoId) ? SUCCESS : FAILURE;
        } else {
            return FAILURE;
        }
    }

    /**
     * Gets the metadata of a video as JSON string.
     *
     * @param videoId Unique identifier of the video to fetch metadata for.
     * @return JSON string with metadata information
     */
    public String getMetaData(int videoId) {
        if (!videoIdVerification(videoId))
            return FAILURE;

        String metaName = databaseManager.getMetaName(videoId);

        if (metaName == null)
            return FAILURE;

        String filePath = LocationConfig.META_DIR + File.separator + account.getId() + "_" +
                metaName + Metadata.FILE_EXTENTION;

        try {
            Metadata metadata = new Metadata(new String(Files.readAllBytes(Paths.get(filePath))));
            return metadata.getAsJSON();
        } catch (IOException e) {
            Logger.getGlobal().warning("An error occured reading metadata file " + filePath);
            return FAILURE;
        }
    }


    //TODO: Implement Method
    /**
     * Checks if specific videoId matches with the corresponding user, which wants to access the video/information
     *
     * @param videoId to check for the specific user
     * @return        returns true if the requesting account has a verified connection to the videoId,
     *                else returns false
     */
    private boolean videoIdVerification(int videoId) {
        return databaseManager.videoId_UserId_Matching(videoId);

    }
}
