package edu.kit.informatik.pcc.service.videoprocessing.chain.decryption;

import edu.kit.informatik.pcc.service.data.LocationConfig;
import org.junit.*;

import java.io.File;

/**
 * Test for the decryptor classes.
 *
 * @author Josh Romanowski
 * Created by Josh Romanowski on 18.01.2017.
 */
@Ignore
public class DecryptTest {

    private File encKey;
    private File encVid;
    private File decVid;
    private File encMeta;
    private File decMeta;
    private Decryptor decryptor;

    @Before
    public void setUp() {
        encKey = new File(LocationConfig.TEST_RESOURCES_DIR + File.separator + "KEY_1487198226374.key");
        encVid = new File(LocationConfig.TEST_RESOURCES_DIR + File.separator + "VIDEO_1487198226374.mp4");
        decVid = new File(LocationConfig.OUTPUT_DIR + File.separator + "decVid.mp4");
        encMeta = new File(LocationConfig.TEST_RESOURCES_DIR + File.separator + "META_1487198226374.json");
        decMeta = new File(LocationConfig.OUTPUT_DIR + File.separator + "decMetaForDecTest.json");
        decryptor = new Decryptor();
    }

    @Test
    public void nullTest() {
        Assert.assertFalse(decryptor.decrypt(null, encKey, encMeta, decVid, decMeta));
    }

    @Test
    public void validTest() {
        Assert.assertTrue(decryptor.decrypt(encVid, encKey, encMeta, decVid, decMeta));
    }

    @After
    public void cleanUp() {
        if (decVid.exists())
            decVid.delete();
        if (decMeta.exists())
            decMeta.delete();
    }
}
