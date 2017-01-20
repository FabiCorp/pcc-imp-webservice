package edu.kit.informatik.pcc.service.videoprocessing;

import edu.kit.informatik.pcc.service.data.Account;

import javax.ws.rs.container.AsyncResponse;
import java.io.IOException;
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

    // attributes

    /**
     * Size of the threapool used.
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

    // constructors

    /**
     * Sets up the queue and the executor. Defines what should happen if the queue is full
     * and a task is being inserted.
     */
    private VideoProcessingManager() {
        /*
      Queue that holds the tasks that are not yet executed.
     */
        BlockingQueue<Runnable> queue = new LinkedBlockingDeque<>(QUEUE_SIZE);
        executor = new ThreadPoolExecutor(POOL_SIZE, POOL_SIZE, 30,
                TimeUnit.SECONDS, queue, new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                VideoProcessingChain chain = (VideoProcessingChain) r;
                chain.cleanUp();

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

    // methods

    /**
     * Adds a new task to the queue, which gets executed as soon as resources get free.
     * Gives response via the response object.
     *
     * @param video     Uploaded video.
     * @param metadata  Uploaded metadata.
     * @param key       Uploaded key.
     * @param account   User account who uploaded the video.
     * @param videoName Video name of the uploaded video.
     * @param response  Object use for giving responses.
     */
    public void addTask(InputStream video, InputStream metadata, InputStream key,
                        Account account, String videoName, AsyncResponse response) {
        addTask(video, metadata, key, account, videoName, response, VideoProcessingChain.Chain.SIMPLE);
    }

    /**
     * TODO: javadoc
     *
     * @param video
     * @param metadata
     * @param key
     * @param account
     * @param videoName
     * @param response
     * @param chainType
     */
    protected void addTask(InputStream video, InputStream metadata, InputStream key,
                           Account account, String videoName, AsyncResponse response, VideoProcessingChain.Chain chainType) {
        if (response == null) {
            Logger.getGlobal().warning("No response given.");
        }

        if (video == null || metadata == null || key == null
                || account == null || videoName == null) {
            Logger.getGlobal().warning("Not all inputs were given correctly");
            response.resume("Not all inputs were given correctly");
            return;
        }

        VideoProcessingChain chain;

        try {
            chain = new VideoProcessingChain(video, metadata, key, account, videoName, response, chainType);
        } catch (IOException e) {
            Logger.getGlobal().warning("Setting up for editing video "
                    + videoName + " of user " + account.getId() + " failed. Processing aborted");
            response.resume("Setting up for editing video " + videoName + " failed. Processing aborted");
            return;
        }

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
