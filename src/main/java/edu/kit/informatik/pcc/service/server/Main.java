package edu.kit.informatik.pcc.service.server;

import edu.kit.informatik.pcc.service.data.LocationConfig;
import edu.kit.informatik.pcc.service.videoprocessing.VideoProcessingManager;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.*;

/**
 * Main entry point for the webserver.
 * Starts the server and sets up proxy for handling requests as well as
 * video processing manager for processing videos.
 *
 * @author Josh Romanowski, Fabian Wenzel
 */
public class Main {

    // constants
    private static final int PORT = 2222;
    private static final String REQUEST_LOCATION = "edu.kit.informatik.pcc.service.server";

    /* #############################################################################################
     *                                  attributes
     * ###########################################################################################*/

    /**
     * Server instance used for http access.
     */
    private static Server server;

    /* #############################################################################################
     *                                  methods
     * ###########################################################################################*/

    /**
     * Sets the server up and starts it.
     *
     * @param args no args
     */
    public static void main(String[] args) {
        // database constants
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(LocationConfig.RESOURCES_DIR +
                    File.separator + "connection.properties"));
        } catch (IOException e) {
            Logger.getGlobal().severe("Properties file could not be load correctly!");
        }
        startServer();
    }

    /**
     * Stops the server if it is still running. Also shuts down the video processing chain.
     */
    public static void stopServer() {

        if (server.isStopped()) {
            Logger.getGlobal().info("Server is already stopped");
            return;
        }

        Logger.getGlobal().info("Stopping Server");

        // shutdown video processing
        VideoProcessingManager.getInstance().shutdown();

        try {
            // shutdown server
            server.stop();
        } catch (Exception e) {
            Logger.getGlobal().warning("Stopping the server failed.");
        }

        // finish log
        for (Handler handler : Logger.getGlobal().getHandlers()) {
            handler.close();
        }
    }

    /* #############################################################################################
     *                                  helper methods
     * ###########################################################################################*/

    /**
     * Starts the server and sets up the proxy to handle requests.
     * Also creates all necessary directories for the server and sets up the logger.
     *
     * @return Returns if starting the server is successful
     */
    private static boolean startServer() {

        if (!setupDirectories() || !setupLogger()) {
            System.out.println("Setup failed");
            return false;
        }

        Logger.getGlobal().info("Starting Server");
        ResourceConfig config = new ResourceConfig();
        config.packages(REQUEST_LOCATION); //where to search for rest requests
        config.register(MultiPartFeature.class); //register feature for file upload (multipartfeature)
        ServletHolder servlet = new ServletHolder(new ServletContainer(config)); // add the config to the servletholder

        server = new Server(PORT);
        ServletContextHandler context = new ServletContextHandler(server, "/");
        context.addServlet(servlet, "/*");

        if (server.isStarted()) {
            Logger.getGlobal().info("Server already started");
            return true;
        }

        try {
            server.start();
            server.join();
        } catch (InterruptedException e) {
            Logger.getGlobal().warning("Server was interrupted during execution");
            return false;
        } catch (Exception e) {
            Logger.getGlobal().severe("Error in server");
            stopServer();
            return false;
        }
        return true;
    }

    /**
     * Sets up the logger so that there is one handling writing error logs and one
     * handler writing the full log.
     *
     * @return Returns whether setting up the logger was successful or not.
     */
    private static boolean setupLogger() {
        Logger logger = Logger.getGlobal();
        try {
            // output file = "error.log", max size = 1 MByte, 1 single logfile
            Handler fileErrorHandler = new FileHandler(
                    LocationConfig.LOG_DIR + File.separator + "error.log", 1024000, 1);
            fileErrorHandler.setFormatter(new SimpleFormatter());
            fileErrorHandler.setLevel(Level.WARNING);

            Handler fileInfoHandler = new FileHandler(
                    LocationConfig.LOG_DIR + File.separator + "server.log", 1024000, 1);
            fileInfoHandler.setFormatter(new SimpleFormatter());
            fileInfoHandler.setLevel(Level.INFO);

            logger.addHandler(fileInfoHandler);
            logger.addHandler(fileErrorHandler);
        } catch (SecurityException | IOException e) {
            return false;
        }
        logger.info("Logger setup");
        return true;
    }

    /**
     * Creates all necessary directions as long as they were not existing before.
     * Also empties the tmp folder.
     *
     * @return Returns whether setting was successful or not.
     */
    private static boolean setupDirectories() {
        boolean ret = true;

        File[] dirs = new File[]{
                new File(LocationConfig.ANONYM_VID_DIR),
                new File(LocationConfig.META_DIR),
                new File(LocationConfig.TEMP_DIR),
                new File(LocationConfig.LOG_DIR)
        };

        for (File dir : dirs) {
            if (!dir.exists()) {
                ret &= dir.mkdir();
            }
        }

        //delete all temp files
        File[] tempFiles = new File(LocationConfig.TEMP_DIR).listFiles();
        if (tempFiles == null) {
            return false;
        }

        for (File file : tempFiles) {
            ret &= file.delete();
        }

        return ret;
    }
}
