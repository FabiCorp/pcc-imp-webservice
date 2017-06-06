package edu.kit.informatik.pcc.service.videoprocessing;

import java.io.*;

/**
 * Created by david on 06.06.17.
 * @author David Laubenstein, Fabian Wenzel
 */
public class VideoFileManager {

    /**
     * Context which stores the information needed to process the videos.
     */
    private EditingContext context;

    public VideoFileManager(EditingContext context) {
        this.context = context;
    }

    /**
     * Saves all provided inputs to their temporary location on the server.
     *
     * @param video    Uploaded video file as stream.
     * @param metadata Uploaded metadata file as stream.
     * @param key      Uploaded SecretKey file as stream.
     * @throws IllegalArgumentException in case some of the inputs could not be saved correctly and completely.
     */
    public void saveTempFiles(InputStream video, InputStream metadata, InputStream key)
            throws IllegalArgumentException {

        try {

            //create output files
            FileOutputStream videoOut = new FileOutputStream(context.getEncVid());
            FileOutputStream metaOut = new FileOutputStream(context.getEncMetadata());
            FileOutputStream keyOut = new FileOutputStream(context.getEncKey());

            //save files
            saveFile(video, videoOut);
            saveFile(metadata, metaOut);
            saveFile(key, keyOut);
        } catch (IOException e) {
            cleanUp();
            throw new IllegalArgumentException();
        }
    }
//TODO: Javadoc
    /**
     * @param video
     * @param metadata
     * @throws IllegalArgumentException
     */
    public void safeDecFiles(InputStream video, InputStream metadata)
        throws IllegalArgumentException {

        try {

            //create output files
            FileOutputStream videoOut = new FileOutputStream(context.getDecVid());
            FileOutputStream metaOut = new FileOutputStream(context.getDecMetadata());

            //safe files
            saveFile(video, videoOut);
            saveFile(metadata, metaOut);
        } catch (IOException e) {
            cleanUp();
            throw new IllegalArgumentException();
        }
    }

    /**
     * Saves a file provided to a location provided.
     *
     * @param input  Input stream passing the file's data.
     * @param output Output stream saving to the new file.
     * @throws IOException in case writing or reading fails.
     */
    private void saveFile(InputStream input, OutputStream output) throws IOException {
        int read;
        byte[] bytes = new byte[1024];

        try {
            while ((read = input.read(bytes)) != -1) {
                output.write(bytes, 0, read);
            }
        } finally {
            input.close();
            output.flush();
            output.close();
        }
    }

    /**
     * Cleans up all files and further context created for the video processing.
     */
    void cleanUp() {
        // atm only calls deleteTempFiles but is separated from it in case further functionality
        // becomes necessary for cleaning up.
        deleteTempFiles(context);
    }

    /**
     * Deletes all the temporary files that were created while processing the video.
     *
     * @param context Context that contains all files used.
     */
    public void deleteTempFiles(EditingContext context) {
        for (File file : context.getAllTempFiles()) {
            if (file.exists()) {
                file.delete();
            }
        }
    }
}