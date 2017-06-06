package edu.kit.informatik.pcc.service.videoprocessing.chain.anonymization;

import edu.kit.informatik.pcc.service.data.LocationConfig;
import edu.kit.informatik.pcc.service.data.VideoInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * Test for the OpenCv anonymizer.
 *
 * @author Josh Romanowski
 *         Created by Josh Romanowski on 18.01.2017.
 */
public class OpenCvAnonymizeTest {
    private AAnonymizer anonymizer;
    private File input;
    private File output;

    @Before
    public void setUp() {
        anonymizer = new OpenCVAnonymizer();
        input = new File(LocationConfig.TEST_RESOURCES_DIR + File.separator + "decVid.mp4");
        output = new File(LocationConfig.OUTPUT_DIR + File.separator + "output" + VideoInfo.FILE_EXTENSION);
    }

    @Test
    public void nullTest() {
        Assert.assertFalse(anonymizer.anonymize(null, output));
        Assert.assertFalse(anonymizer.anonymize(input, null));
        Assert.assertFalse(anonymizer.anonymize(null, null));
    }

    // ignore for performance when building
    @Test
    public void validTest() {
        Assert.assertTrue(anonymizer.anonymize(input, output));
    }

    @Test
    public void noVideoTest() {
        input = new File(LocationConfig.TEST_RESOURCES_DIR + File.separator + "encMeta.json");
        Assert.assertFalse(anonymizer.anonymize(input, output));
    }
}
