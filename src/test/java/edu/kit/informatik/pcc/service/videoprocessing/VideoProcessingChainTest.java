package edu.kit.informatik.pcc.service.videoprocessing;

import edu.kit.informatik.pcc.service.data.*;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.TimeoutHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @author Josh Romanowski
 */
public class VideoProcessingChainTest {

    private VideoProcessingChain chain;
    private DatabaseManager databaseManager;

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

    @Before
    public void setUp() {
        response = setupResponse();

        vidFile = new File(LocationConfig.TEST_RESOURCES_DIR + File.separator + "VIDEO_1487198226374" + VideoInfo.FILE_EXTENSION);
        metaFile = new File(LocationConfig.TEST_RESOURCES_DIR + File.separator + "META_1487198226374" + Metadata.FILE_EXTENSION);
        keyFile = new File(LocationConfig.TEST_RESOURCES_DIR + File.separator + "KEY_1487198226374.key");

        videoName = "1487198226374";
        account = Mockito.mock(Account.class);

        Mockito.when(account.getId()).thenReturn(-1);

        databaseManager = new DatabaseManager(account);

        setupStreams();
    }

    @After
    public void cleanUp() {
        List<VideoInfo> videos = databaseManager.getVideoInfoList();
        for (VideoInfo video : videos) {
            databaseManager.deleteVideoAndMeta(video.getVideoId());
        }

        File testedVid = new File(
                LocationConfig.ANONYM_VID_DIR + File.separator + "-1_" + videoName + VideoInfo.FILE_EXTENSION);
        File testedMeta = new File(
                LocationConfig.META_DIR + File.separator + "-1_" + videoName + "_meta" + Metadata.FILE_EXTENSION);

        if (testedVid.exists())
            testedVid.delete();
        if (testedMeta.exists())
            testedMeta.delete();
    }

    @Test
    @Ignore
    public void emptyChainTest() {
        testChainType(VideoProcessingChain.Chain.EMPTY, 1);
    }
    @Test
    public void sgxChainTest() {
        testChainType(VideoProcessingChain.Chain.SGX, 1);
    }

    @Test
    @Ignore
    public void simpleChainTest() {
        testChainType(VideoProcessingChain.Chain.SIMPLE, 5);
    }

    @Test
    @Ignore
    public void normalChainTest() {
        testChainType(VideoProcessingChain.Chain.NORMAL, 120);
    }

    @Test
    @Ignore
    public void pythonChainTest() {
        testChainType(VideoProcessingChain.Chain.PYTHON, 500);
    }

    private void testChainType(VideoProcessingChain.Chain chainType, long timeout) {
        // test setup
        lock = new CountDownLatch(1);
        try {
            EditingContext context = new EditingContext(account, videoName);
            chain = new VideoProcessingChain(vidInput, metaInput, context , response, chainType);
        } catch (IllegalArgumentException e) {
            Assert.fail();
        }

        // test

        new Thread(new Runnable() {
            @Override
            public void run() {
                chain.run();
            }
        }).start();


        // check result
        try {
            lock.await(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Logger.getGlobal().warning("Interrupted while waiting for response");
        }

        Assert.assertEquals(responseString, "Finished editing video");
    }

    // helper functions

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