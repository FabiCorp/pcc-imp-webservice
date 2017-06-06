package edu.kit.informatik.pcc.service.videoprocessing;

import edu.kit.informatik.pcc.service.data.Account;
import edu.kit.informatik.pcc.service.data.LocationConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.TimeoutHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by Josh Romanowski on 18.01.2017.
 */
public class VideoProcessingManagerTest {

    private VideoProcessingManager manager;
    private String responseString;

    private File vidFile;
    private File metaFile;
    private File keyFile;
    private String videoName;

    private FileInputStream vidInput;
    private FileInputStream metaInput;
    private FileInputStream keyInput;

    private CountDownLatch lock;

    @Mock
    private Account account;
    private AsyncResponse response;

    // setup

    @Before
    public void setUp() {
        manager = VideoProcessingManager.getInstance();
        response = setupResponse();

        vidFile = new File(LocationConfig.TEST_RESOURCES_DIR + File.separator + "encVid.mp4");
        metaFile = new File(LocationConfig.TEST_RESOURCES_DIR + File.separator + "encMeta.json");
        keyFile = new File(LocationConfig.TEST_RESOURCES_DIR + File.separator + "encKey.txt");

        videoName = "testVideo";
        account = Mockito.mock(Account.class);
    }

    // tests

    @Test
    public void addNullTaskTest() throws InterruptedException {
        // test setup
        lock = new CountDownLatch(1);

        // test
        manager.addTask(null, null, null, null, response, VideoProcessingChain.Chain.EMPTY);

        // check result
        lock.await(2000, TimeUnit.MILLISECONDS);
        Assert.assertEquals(responseString, "Not all inputs were given correctly");
    }

    @Test
    public void addValidTaskTest() throws InterruptedException {
        //test setup
        lock = new CountDownLatch(1);
        Mockito.when(account.getId()).thenReturn(-1);
        setupStreams();

        // test
        manager.addTask(vidInput, metaInput, account, videoName, response, VideoProcessingChain.Chain.EMPTY);

        // check result
        lock.await(2000, TimeUnit.MILLISECONDS);
        Assert.assertEquals(responseString, "Finished editing video");
    }

    @Test
    public void shutdownTest() throws InterruptedException {
        // test setup
        lock = new CountDownLatch(1);
        setupStreams();

        // test
        manager.shutdown();
        manager.addTask(vidInput, metaInput, account, videoName, response, VideoProcessingChain.Chain.EMPTY);

        // check result
        lock.await(2000, TimeUnit.MILLISECONDS);
        Assert.assertTrue(responseString.endsWith("Processing module is shut down."));
    }

    // helper methods

    private AsyncResponse setupResponse() {
        return new AsyncResponse() {
            @Override
            public boolean resume(Object response) {
                responseString = (String) response;
                lock.countDown();
                return true;
            }

            @Override
            public boolean resume(Throwable response) {
                responseString = response.getMessage();
                lock.countDown();
                return true;
            }

            @Override
            public boolean cancel() {
                return false;
            }

            @Override
            public boolean cancel(int retryAfter) {
                return false;
            }

            @Override
            public boolean cancel(Date retryAfter) {
                return false;
            }

            @Override
            public boolean isSuspended() {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public boolean setTimeout(long time, TimeUnit unit) {
                return false;
            }

            @Override
            public void setTimeoutHandler(TimeoutHandler handler) {

            }

            @Override
            public Collection<Class<?>> register(Class<?> callback) {
                return null;
            }

            @Override
            public Map<Class<?>, Collection<Class<?>>> register(Class<?> callback, Class<?>[] callbacks) {
                return null;
            }

            @Override
            public Collection<Class<?>> register(Object callback) {
                return null;
            }

            @Override
            public Map<Class<?>, Collection<Class<?>>> register(Object callback, Object... callbacks) {
                return null;
            }
        };
    }

    private void setupStreams() {
        try {
            vidInput = new FileInputStream(vidFile);
            metaInput = new FileInputStream(metaFile);
            keyInput = new FileInputStream(keyFile);
        } catch (FileNotFoundException e) {
            Assert.fail();
        }
    }
}
