package edu.kit.informatik.pcc.service.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Logger;

/**
 * Datacontainer for video metadata.
 * Currently offers no additional information to the metadata served by the app.
 * This class allows to add additional metadata concerning the editing on the service.
 *
 * @author David Laubenstein, Fabian Wenzel, Josh Romanowski
 */
public class Metadata {

    /**
     * File extension of metadata files.
     */
    public final static String FILE_EXTENSION = ".json";

    // JSON keys
    private final static String JSON_KEY_DATE = "date";
    private final static String JSON_KEY_TRIGGER_TYPE = "triggerType";
    private final static String JSON_KEY_TRIGGER_FORCE_X = "triggerForceX";
    private final static String JSON_KEY_TRIGGER_FORCE_Y = "triggerForceY";
    private final static String JSON_KEY_TRIGGER_FORCE_Z = "triggerForceZ";
    private final static String JSON_KEY_EDITING_DATE = "editingDate";

    /* #############################################################################################
     *                                  attributes
     * ###########################################################################################*/

    /**
     * Data of the video recording.
     */
    private long date;
    /**
     * Trigger type of the recording.
     */
    private String triggerType;
    /**
     * G-Force values at the moment of recording.
     */
    private float[] gForce;

    private long editingDate = -1;

    /* #############################################################################################
     *                                  constructor
     * ###########################################################################################*/

    /**
     * Creates a metadata container from a JSON string.
     *
     * @param json JSON string to fetch data from.
     * @throws IllegalArgumentException when json string is not valid
     */
    public Metadata(String json) throws IllegalArgumentException{

        // retrieve json data
        try {
            JSONObject metadata = new JSONObject(json);
            this.date = metadata.getLong(JSON_KEY_DATE);
            this.triggerType = metadata.getString(JSON_KEY_TRIGGER_TYPE);
            this.gForce = new float[3];
            this.gForce[0] = (float) metadata.getDouble(JSON_KEY_TRIGGER_FORCE_X);
            this.gForce[1] = (float) metadata.getDouble(JSON_KEY_TRIGGER_FORCE_Y);
            this.gForce[2] = (float) metadata.getDouble(JSON_KEY_TRIGGER_FORCE_Z);

            if (metadata.has(JSON_KEY_EDITING_DATE)) {
                this.editingDate = metadata.getLong(JSON_KEY_EDITING_DATE);
            }
        } catch (JSONException e) {
            Logger.getGlobal().warning("Error reading metadata json");
            throw new IllegalArgumentException();
        }
    }
    /**
     * Creates new metadata from given values.
     *
     * @param date        Date of the recording.
     * @param triggerType Trigger type of the recording.
     * @param gForce      G-force values in the moment of the recording.
     */
    public Metadata(long date, String triggerType, float[] gForce) {
        this.date = date;
        this.triggerType = triggerType;
        this.gForce = gForce;
    }

    /* #############################################################################################
     *                                  methods
     * ###########################################################################################*/

    public String getAsJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put(JSON_KEY_DATE, this.date);
            json.put(JSON_KEY_TRIGGER_TYPE, this.triggerType);
            json.put(JSON_KEY_TRIGGER_FORCE_X, this.gForce[0]);
            json.put(JSON_KEY_TRIGGER_FORCE_Y, this.gForce[1]);
            json.put(JSON_KEY_TRIGGER_FORCE_Z, this.gForce[2]);

            if (editingDate != -1) {
                json.put(JSON_KEY_EDITING_DATE, editingDate);
            }
        } catch (JSONException e) {
            Logger.getGlobal().warning("Error creating metadata json");
        }
        return json.toString();
    }

    /* #############################################################################################
     *                                  getter/setter
     * ###########################################################################################*/

    public void setEditingDate(long editingDate) {
        this.editingDate = editingDate;
    }
}
