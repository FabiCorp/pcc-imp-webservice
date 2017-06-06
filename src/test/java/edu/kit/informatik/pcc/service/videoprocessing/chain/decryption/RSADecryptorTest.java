package edu.kit.informatik.pcc.service.videoprocessing.chain.decryption;

import edu.kit.informatik.pcc.service.data.LocationConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.crypto.SecretKey;
import java.io.File;

/**
 * Explicitly tests the RSA Decryptor only
 *
 * @author Josh Romanowski
 */
@Ignore
public class RSADecryptorTest {
    private static final String KEY_FILE = LocationConfig.TEST_RESOURCES_DIR + File.separator + "KEY_1487198226374.key";
    private static final String NO_KEY_FILE = LocationConfig.TEST_RESOURCES_DIR + File.separator + "META_1487198226374.json";

    private RSADecryptor decryptor;

    @Before
    public void setUp() {
        decryptor = new RSADecryptor();
    }

    @Test
    public void nullTest() {
        Assert.assertNull(decryptor.decrypt(null));
    }

    @Test
    public void invalidFileTest() {
        Assert.assertNull(decryptor.decrypt(new File(NO_KEY_FILE)));
    }

    @Test
    public void validTest() {
        SecretKey key = decryptor.decrypt(new File(KEY_FILE));
        Assert.assertNotNull(key);
        Assert.assertTrue(key.getAlgorithm().startsWith("AES"));
    }
}
