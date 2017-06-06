package edu.kit.informatik.pcc.service.data;

import org.json.JSONObject;
import org.junit.*;

import java.util.ArrayList;
import java.util.Base64;

import static edu.kit.informatik.pcc.service.server.ServerProxyTest.setFinalStatic;

/**
 * @author David Laubenstein
 * Created by David Laubenstein on 1/18/17.
 */
public class DatabaseManagerTest {
    private final String OWN_UUID = "102394871234";
    private Account account;
    private DatabaseManager dm;
    private boolean registered = false;
    private String bytes;


    @BeforeClass
    public static void setUpBeforeClass() {
    }

    @AfterClass
    public static void cleanUpAfterClass() {

    }

    @Before
    public void setUpBefore() {
        String json = "";
        String mail = "\"testEMAIL@123schonVorbei.com\"";
        String password = "\"testPasswordForUni\"";
        String DATE = "\"123\"";
        String TRIGGER_TYPE = "\"23\"";
        String G_FORCE_X = "30.33333";
        String G_FORCE_Y = "40";
        String G_FORCE_Z = "50";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mail", mail);
        jsonObject.put("password", password);
        json = jsonObject.toString();
        account = new Account();
        byte[] bytesB = new byte[] { (byte)0xe0, 0x4f, (byte)0xd0, 0x20, (byte)0xea, 0x3a, 0x69, 0x10, (byte)0xa2, (byte)0xd8,
                0x08, 0x00, 0x2b, 0x30, 0x30, (byte)0x9d };
        bytes = Base64.getEncoder().encodeToString(bytesB);

        dm = new DatabaseManager(account);
        try {
            setFinalStatic(LocationConfig.class.getDeclaredField("META_DIR"), LocationConfig.TEST_RESOURCES_DIR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void registerTest() {
        registered = dm.register(OWN_UUID, bytes);
        account.setId(dm.getAccountId());
        Assert.assertTrue(registered);
    }

    @Test
    public void getAccountIdTest() {
        registered = dm.register(OWN_UUID, bytes);
        account.setId(dm.getAccountId());
        Assert.assertEquals("account.getId() is not equals dm.getAccountId()", account.getId(), dm.getAccountId());
    }

    /**
     * same as verifyAccountTest, so we will not duplicate them
     */
    @Test
    public void isVerifiedTest() {
        registered = dm.register(OWN_UUID, bytes);
        account.setId(dm.getAccountId());
        Assert.assertTrue(!dm.isVerified());
        dm.verifyAccount(OWN_UUID);
        Assert.assertTrue(dm.isVerified());
    }

    @Test
    public void saveProcessedVideoAndMetaTest() {
        registered = dm.register(OWN_UUID, bytes);
        account.setId(dm.getAccountId());
        String videoName = "videoTest123";
        String metaName = "metaTest123";
        // save video
        dm.saveProcessedVideoAndMeta(videoName, metaName);
        // getVideoInfo
        VideoInfo vI = dm.getVideoInfo(dm.getVideoIdByName(videoName));
        Assert.assertTrue(vI.getName().equals(videoName));
        // getMetaInfo
        Assert.assertTrue((dm.getMetaName(dm.getVideoIdByName(videoName))).equals(metaName));
        // delete Video
        dm.deleteVideoAndMeta(dm.getVideoIdByName(videoName));
    }

    @Test
    public void getVideoInfoTest() {
        registered = dm.register(OWN_UUID, bytes);
        account.setId(dm.getAccountId());
        String videoName = "videoTest123321";
        String metaName = "videoTest123321";
        // save Video
        dm.saveProcessedVideoAndMeta(videoName, metaName);
        // save VideoInfo object
        int id = dm.getVideoIdByName(videoName);
        VideoInfo vI = dm.getVideoInfo(id);
        Assert.assertTrue(vI.getName().equals(videoName));
        Assert.assertTrue(vI.getVideoId() == dm.getVideoIdByName(videoName));
        dm.deleteVideoAndMeta(dm.getVideoIdByName(videoName));
    }

    @Test
    public void getVideoIdByNameTest() {
        registered = dm.register(OWN_UUID, bytes);
        account.setId(dm.getAccountId());
        dm.saveProcessedVideoAndMeta("getVideoIdByNameTestVIDEO", "getVideoIdByNameTestMETA");
        Assert.assertTrue(dm.deleteVideoAndMeta(dm.getVideoIdByName("getVideoIdByNameTestVIDEO")));
    }

    @Test
    public void getVideoInfoListTest() {
        registered = dm.register(OWN_UUID, bytes);
        account.setId(dm.getAccountId());
        String videoName1 = "videoGetVideoInfoListTest";
        String videoName2 = "video2GetVideoInfoListTest";
        String metaName1 = "meta1GetVideoInfoListTest";
        String metaName2 = "meta2GetVideoInfoListTest";
        boolean check1 = false;
        boolean check2= false;
        // create 2 testVideos
        dm.saveProcessedVideoAndMeta(videoName1, metaName1);
        dm.saveProcessedVideoAndMeta(videoName2, metaName2);

        // save List in ArrayList<VideoInfo>
        ArrayList<VideoInfo> arrayList= dm.getVideoInfoList();
        // check, if both videos are in list
        for (VideoInfo listElement : arrayList) {
            if (listElement.getName().equals(videoName1) && listElement.getVideoId() == dm.getVideoIdByName(videoName1)) {
                check1 = true;
            }
            if (listElement.getName().equals(videoName2) && listElement.getVideoId() == dm.getVideoIdByName(videoName2)) {
                check2 = true;
            }
        }
        // if length of ArrayList = 2 and both videos are found, test success
        Assert.assertTrue(check1 && check2 && arrayList.size() == 2);
        // delete both videos to delete Account
        dm.deleteVideoAndMeta(dm.getVideoIdByName(videoName1));
        dm.deleteVideoAndMeta(dm.getVideoIdByName(videoName2));
    }

    @Test
    public void deleteVideoAndMetaTest() {
        registered = dm.register(OWN_UUID, bytes);
        account.setId(dm.getAccountId());
        boolean create  = dm.saveProcessedVideoAndMeta("test","test");
        boolean delete = dm.deleteVideoAndMeta(dm.getVideoIdByName("test"));
        Assert.assertTrue("insert video or delete Video not working!", create && delete);
    }

    @Test
    public void getMetaNameTest() {
        registered = dm.register(OWN_UUID, bytes);
        account.setId(dm.getAccountId());
        String videoName = "videoGetMetaNameByVideoId";
        String metaName = "metaGetMetaNameByVideoId";
        // save video in database
        dm.saveProcessedVideoAndMeta(videoName, metaName);
        // if metaName equals the info in database, test passes
        Assert.assertTrue(dm.getMetaName(dm.getVideoIdByName(videoName)).equals(metaName));
        // delete Video, so that the account can be deleted
        dm.deleteVideoAndMeta(dm.getVideoIdByName(videoName));
    }

    @Test
    public void setMailTest() {
        Assert.assertTrue(dm.setMail("newTESTMAIL@hlminop.de"));
    }

    @Test
    public void setPasswordTest() {
       Assert.assertTrue(dm.setPassword("passwordTestabc"));
    }

    @Test
    public void deleteAccountTest() {
        Assert.assertTrue(dm.deleteAccount());
    }

    @Test
    public void authenticateTest() {
        registered = dm.register(OWN_UUID, bytes);
        account.setId(dm.getAccountId());
        account.hashPassword(Base64.getDecoder().decode(dm.getSalt()));
        dm.setPassword(account.getPasswordHash());
        Assert.assertTrue(dm.authenticate());
    }

    @Test
    public void isMailExistingTest() {
        registered = dm.register(OWN_UUID, bytes);
        account.setId(dm.getAccountId());
        Assert.assertTrue(dm.isMailExisting(account.getMail()));
        Assert.assertFalse(dm.isMailExisting("mailWhichIsNotExisting@notExisting.NO"));
    }

    @Test
    public void getSaltTest() {
        registered = dm.register(OWN_UUID, bytes);
        account.setId(dm.getAccountId());
        Assert.assertEquals(dm.getSalt(), bytes);
    }

    @After
    public void cleanUpAfter() {
        dm.deleteAccount();
        dm = null;
    }
}
