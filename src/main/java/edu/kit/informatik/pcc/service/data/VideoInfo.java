package edu.kit.informatik.pcc.service.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Logger;

/**
 * Datacontainer for video information
 *
 * @author David Laubenstein, Fabian Wenzel
 */
public class VideoInfo {

    /**
     * File extension of video files.
     */
    public static final String FILE_EXTENSION = ".mp4";

    // JSON keys
    private static final String JSON_KEY_NAME = "name";
    private static final String JSON_KEY_ID = "id";

    /* #############################################################################################
     *                                  attributes
     * ###########################################################################################*/

    /**
     * Unique video identifier that maps the one in the database.
     */
    private int videoId;
    /**
     * Name of a video.
     */
    private String videoName;

    /* #############################################################################################
     *                                  constructors
     * ###########################################################################################*/

    /**
     * constructor
     *
     * @param videoId   the id of the video
     * @param videoName the name of the video
     */
    public VideoInfo(int videoId, String videoName) {
        this.videoId = videoId;
        this.videoName = videoName;
    }

    /* #############################################################################################
     *                                  methods
     * ###########################################################################################*/

    /**
     * Creates a JSON string from a account
     *
     * @return JSON String of videoInfo object
     */
    public String getAsJson() {
        JSONObject json = new JSONObject();
        try {
            json.put(JSON_KEY_NAME, this.videoName);
            json.put(JSON_KEY_ID, this.videoId);
        } catch (JSONException e) {
            Logger.getGlobal().warning("Error while creating video info json");
        }
        return json.toString();
    }

    /* #############################################################################################
     *                                  getter/setter
     * ###########################################################################################*/

    public String getName() {
        return videoName;
    }

    public int getVideoId() {
        return videoId;
    }
}
