package edu.kit.informatik.pcc.service.videoprocessing;

import edu.kit.informatik.pcc.service.data.Account;

import javax.ws.rs.container.AsyncResponse;
import java.io.File;
import java.io.InputStream;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Controller for editing videos. Takes editing requests and puts them into it's queue.
 * When resources are free it takes the taks and executes the processing.
 *
 * @author Josh Romanowski
 */
public class VideoProcessingManager {
//TODO: can we have a nicer solution for editing context
    /* #############################################################################################
     *                                  attributes
     * ###########################################################################################*/

    /**
     * Size of the thread pool used.
     */
    private final static int POOL_SIZE = 4;
    /**
     * Maximum amount of accepted tasks.
     */
    private final static int QUEUE_SIZE = 10;
    /**
     * Instance of the VideoProcessingManager used for Singleton behaviour.
     */
    private static VideoProcessingManager instance;

    /**
     * Executor that controls the execution of the tasks.
     */
    private ExecutorService executor;

    private VideoFileManager videoFileManager;


    /* #############################################################################################
     *                                  constructors
     * ###########################################################################################*/

    /**
     * Sets up the queue and the executor. Defines what should happen if the queue is full
     * and a task is being inserted.
     */
    private VideoProcessingManager() {
        BlockingQueue<Runnable> queue = new LinkedBlockingDeque<>(QUEUE_SIZE);
        executor = new ThreadPoolExecutor(POOL_SIZE, POOL_SIZE, 30,
                TimeUnit.SECONDS, queue, new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                VideoProcessingChain chain = (VideoProcessingChain) r;
                videoFileManager.cleanUp();

                String errorMessage = "Inserting video " + chain.getVideoName()
                        + " in queue failed. ";
                if (executor.isShutdown()) {
                    errorMessage += "Processing module is shut down.";
                } else {
                    errorMessage += "Queue is full.";
                }

                Logger.getGlobal().warning(errorMessage);
                chain.getResponse().resume(errorMessage);
            }
        });
    }

    /**
     * Gets the singleton instance of the VideoProcessingManager.
     *
     * @return Returns the singleton instance.
     */
    public static VideoProcessingManager getInstance() {
        return (instance == null) ? instance = new VideoProcessingManager() : instance;
    }

    /* #############################################################################################
     *                                  methods
     * ###########################################################################################*/

    /**
     * Adds a new task to the queue, which gets executed as soon as resources get free.
     * Gives response via the response object. Uses a predefined chain setup.
     *
     * @param video     Uploaded video.
     * @param account   User account who uploaded the video.
     * @param videoName Video name of the uploaded video.
     * @param response  Object use for giving responses.
     */
    public void addTask(InputStream video, InputStream metadata, Account account, String videoName, AsyncResponse response) {
        addTask(video, metadata, account, videoName, response, VideoProcessingChain.Chain.SGX);
    }

    /**
     * Adds a new task to the queue, which gets executed as soon as resources get free.
     * Gives response via the response object. Uses a predefined chain setup.
     *
     * @param video     Uploaded video.
     * @param metadata  Uploaded metadata.
     * @param key       Uploaded key.
     * @param account   User account who uploaded the video.
     * @param videoName Video name of the uploaded video.
     * @param response  Object use for giving responses.
     */
    public void addPersistingTask(InputStream video, InputStream metadata, InputStream key,
                        Account account, String videoName, AsyncResponse response) {
        if (response == null) {
            Logger.getGlobal().warning("No response given.");
            return;
        }

        if (video == null || metadata == null || key == null
                || account == null || videoName == null) {
            Logger.getGlobal().warning("Not all inputs were given correctly");
            response.resume("Not all inputs were given correctly");
            return;
        }

        EditingContext context = new EditingContext(account, videoName);

        try {
            VideoFileManager videoFileManager = new VideoFileManager(context);
            videoFileManager.saveTempFiles(video, metadata, key);
        } catch (IllegalArgumentException e) {
            Logger.getGlobal().warning("Setting up save encrypted video "
                    + videoName + " of user " + account.getId() + " failed. Processing aborted");
            response.resume("Setting up save encrypted video failed. Processing aborted");
            return;
        }
        Logger.getGlobal().warning("Persisting completed successfully");
        response.resume("Persisting completed successfully");
    }

    /**
     * Adds a new task to the queue, which gets executed as soon as resources get free.
     * Gives response via the response object.
     *
     * @param video     Uploaded video.
     * @param account   User account who uploaded the video.
     * @param videoName Video name of the uploaded video without extension.
     * @param response  Object use for giving responses.
     * @param chainType Chain type which will get executed.
     */
    protected void addTask(InputStream video, InputStream metadata, Account account, String videoName, AsyncResponse response,
                           VideoProcessingChain.Chain chainType) {
        if (response == null) {
            Logger.getGlobal().warning("No response given.");
            return;
        }

        if (video == null || account == null || videoName == null) {
            Logger.getGlobal().warning("Not all inputs were given correctly");
            response.resume("Not all inputs were given correctly");
            return;
        }

        EditingContext context = new EditingContext(account, videoName);

        VideoProcessingChain chain;

        try {
            chain = new VideoProcessingChain(video, metadata, context, response, chainType);
        } catch (IllegalArgumentException e) {
            Logger.getGlobal().warning("Setting up for editing video "
                    + videoName + " of user " + account.getId() + " failed. Processing aborted");
            response.resume("Setting up for editing video failed. Processing aborted");
            return;
        }

        Logger.getGlobal().info("Insert video " + videoName + " of user " + account.getId() + " into queue.");
        executor.execute(chain);
    }

    /**
     * Shuts down the queue. Waits 10 Seconds for termination of due tasks.
     */
    public void shutdown() {
        try {
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Logger.getGlobal().info("Got interrupted while waiting for shutdown");
        }
        instance = null;
        Logger.getGlobal().info("Video processing manager stopped");
    }
}
