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
    private VideoFileManager videoFileManager;

    /* #############################################################################################
     *                                  constructors
     * ###########################################################################################*/

    public VideoProcessingChain(InputStream video, InputStream metadata, EditingContext context,
                                AsyncResponse response, Chain chain)
            throws IllegalArgumentException {

        // save response
        this.response = response;

        // create context
        this.context = context;

        // create stages
        stages = new LinkedList<>();
        initChain(chain);

        // save temp files
        videoFileManager = new VideoFileManager(context);
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
                videoFileManager.cleanUp();
                response.resume("Error while editing");
                return;
            }
        }

        videoFileManager.deleteTempFiles(context);

        long endTime = System.currentTimeMillis() - startTime;

        Logger.getGlobal().info("Finished editing video "
                + context.getVideoName() + " of user " + context.getAccount().getId()
                + ". It took " + (endTime / 1000) + " seconds.");

        response.resume("Finished editing video");
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


    /* #############################################################################################
     *                                  getter/setter
     * ###########################################################################################*/

    public AsyncResponse getResponse() {
        return response;
    }

    public String getVideoName() {
        return context.getVideoName();
    }

    /**
     * Enumeration used to make it simple to add new chain types as well as identify existing ones.
     */
    protected enum Chain {
        EMPTY, SIMPLE, NORMAL, PYTHON, SGX
    }
}
