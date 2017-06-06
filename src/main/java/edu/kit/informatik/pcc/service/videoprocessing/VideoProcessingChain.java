package edu.kit.informatik.pcc.service.videoprocessing;

import edu.kit.informatik.pcc.service.data.Account;
import edu.kit.informatik.pcc.service.videoprocessing.chain.anonymization.OpenCVAnonymizer;
import edu.kit.informatik.pcc.service.videoprocessing.chain.anonymization.OpenCVPythonAnonymizer;
import edu.kit.informatik.pcc.service.videoprocessing.chain.decryption.Decryptor;
import edu.kit.informatik.pcc.service.videoprocessing.chain.persistation.FileForwarder;
import edu.kit.informatik.pcc.service.videoprocessing.chain.persistation.Persistor;

import javax.ws.rs.container.AsyncResponse;
import java.io.*;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * The VideoProcessingChain is the core worker of the VideoProcessing module.
 * It does all the work for processing the video. The chain itself is runnable which makes it
 * possible to handle it as a command and therefore enable queueing (used in the manager), logging or undoing.
 *
 * @author Josh Romanowski
 */
public class VideoProcessingChain implements Runnable {

    /* #############################################################################################
     *                                  attributes
     * ###########################################################################################*/

    /**
     * An object used to give the app status updates about the asynchronous processing.
     */
    private AsyncResponse response;
    /**
     * Stages of the chain which will be run through upon execution.
     */
    private LinkedList<IStage> stages;
    /**
     * Context which stores the information needed to process the videos.
     */
    private EditingContext context;
    /**
     * Name of the video being processed.
     */
    private String videoName;

    /* #############################################################################################
     *                                  constructors
     * ###########################################################################################*/

    public VideoProcessingChain(InputStream video, InputStream metadata, InputStream key, Account account,
                                   String videoName, AsyncResponse response, Chain chain)
            throws IllegalArgumentException {

        // save response
        this.response = response;
        this.videoName = videoName;

        // create context
        context = new EditingContext(account, videoName);

        // create stages
        stages = new LinkedList<>();
        initChain(chain);

        // save temp files
        saveTempFiles(video, metadata, key);
    }

    /* #############################################################################################
     *                                  methods
     * ###########################################################################################*/

    @Override
    public void run() {
        Logger.getGlobal().info("Start editing video "
                + context.getVideoName() + " of user " + context.getAccount().getId() + ".");

        long startTime = System.currentTimeMillis();

        //execute all stages
        for (IStage stage : stages) {
            if (!stage.execute(context)) {
                Logger.getGlobal().warning("Stage " + stage.getName() + " failed");
                cleanUp();
                response.resume("Error while editing");
                return;
            }
        }

        deleteTempFiles(context);

        long endTime = System.currentTimeMillis() - startTime;

        Logger.getGlobal().info("Finished editing video "
                + context.getVideoName() + " of user " + context.getAccount().getId()
                + ". It took " + (endTime / 1000) + " seconds.");

        response.resume("Finished editing video");
    }

    /**
     * Cleans up all files and further context created for the video processing.
     */
    void cleanUp() {
        // atm only calls deleteTempFiles but is separated from it in case further functionality
        // becomes necessary for cleaning up.
        deleteTempFiles(context);
    }

    /* #############################################################################################
     *                                  helper methods
     * ###########################################################################################*/

    /**
     * Initializes the chain according to its definition here.
     *
     * @param chain Chain type of the chain to be created.
     */
    private void initChain(Chain chain) {
        switch (chain) {
            case EMPTY:
                break;
            case SIMPLE:
                stages.add(new Decryptor());
                stages.add(new FileForwarder());
                stages.add(new Persistor());
                break;
            case NORMAL:
                stages.add(new Decryptor());
                stages.add(new OpenCVAnonymizer());
                stages.add(new Persistor());
                break;
            case PYTHON:
                stages.add(new Decryptor());
                stages.add(new OpenCVPythonAnonymizer());
                stages.add(new Persistor());
            case SGX:
                stages.add(new OpenCVAnonymizer());
                stages.add(new Persistor());
        }
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
     * Deletes all the temporary files that were created while processing the video.
     *
     * @param context Context that contains all files used.
     */
    private void deleteTempFiles(EditingContext context) {
        for (File file : context.getAllTempFiles()) {
            if (file.exists()) {
                file.delete();
            }
        }
    }


    /* #############################################################################################
     *                                  getter/setter
     * ###########################################################################################*/

    public AsyncResponse getResponse() {
        return response;
    }

    public String getVideoName() {
        return videoName;
    }

    /**
     * Enumeration used to make it simple to add new chain types as well as identify existing ones.
     */
    protected enum Chain {
        EMPTY, SIMPLE, NORMAL, PYTHON, SGX
    }
}
