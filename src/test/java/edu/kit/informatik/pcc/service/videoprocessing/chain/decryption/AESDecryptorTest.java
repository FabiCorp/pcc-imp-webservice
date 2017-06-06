package edu.kit.informatik.pcc.service.videoprocessing.chain.decryption;

import edu.kit.informatik.pcc.service.data.LocationConfig;
import org.junit.*;

import javax.crypto.SecretKey;
import java.io.File;

/**
 * Explicitly tests the AESDecryptor only
 *
 * @author Josh Romanowski
 */
@Ignore
public class AESDecryptorTest {
    private static final String KEY_FILE = LocationConfig.TEST_RESOURCES_DIR + File.separator + "KEY_1487198226374.key";
    private static final String ENC_FILE = LocationConfig.TEST_RESOURCES_DIR + File.separator + "META_1487198226374.json";
    private static final String OUTPUT_FILE = LocationConfig.OUTPUT_DIR + File.separator + "decFile.json";

    private AESDecryptor decryptor;
    private SecretKey secretKey;

    @Before
    public void setUp() {
        decryptor = new AESDecryptor();
        secretKey = new RSADecryptor().decrypt(new File(KEY_FILE));
    }

    @Test
    public void nullTest() {
        Assert.assertFalse(decryptor.decrypt(null, secretKey, new File(OUTPUT_FILE)));
        Assert.assertFalse(decryptor.decrypt(new File(ENC_FILE), null, new File(OUTPUT_FILE)));
        Assert.assertFalse(decryptor.decrypt(new File(ENC_FILE), secretKey, null));
    }

    @Test
    public void validTest() {
        Assert.assertTrue(decryptor.decrypt(new File(ENC_FILE), secretKey, new File(OUTPUT_FILE)));
    }


    @After
    public void cleanUp() {
        File output = new File(OUTPUT_FILE);
        if (output.exists())
            output.delete();
    }
}
