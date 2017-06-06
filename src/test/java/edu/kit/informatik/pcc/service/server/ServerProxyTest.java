package edu.kit.informatik.pcc.service.server;

import edu.kit.informatik.pcc.service.data.*;
import edu.kit.informatik.pcc.service.manager.AccountManager;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Fabian Wenzel
 */
public class ServerProxyTest {
    //TODO: SHORTEN ALL STUFF
    //string for client/request/response
    private final String SUCCESS = "SUCCESS";
    private final String FAILURE = "FAILURE";
    private final String MAIN_ADDRESS = "http://localhost:2222/webservice/";
    private final String ACCOUNT = "account";

    private Account account;
    private String accountJson;
    private String tempAccountJson;
    private String tempUUID = "3456qwe-qw234-2342f";
    private String anonym_dir = LocationConfig.ANONYM_VID_DIR;
    private String meta_dir = LocationConfig.META_DIR;
    private Form form;

    private DatabaseManager databaseManager;
    private Client client;

    //mockup function for LocationConfig fields
    //public because of DatabaseManagerTest
    public static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }

    @Before
    public void setUp() {
        //start server in different thread
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Main.main(new String[0]);
            }
        });
        t.start();

        //wait for server to be up (2 seconds for server but on pc 1 second is enough)
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //create two json objects for testing
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mail", "fabiistkrass@gmail.de");
        jsonObject.put("password", "yochilldeinlife");
        accountJson = jsonObject.toString();

        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("mail", "fabiistababa@yahoo.com");
        jsonObject2.put("password", "ichbindershitfuckyooo");
        tempAccountJson = jsonObject2.toString();

        //setup account and databaseManager for various tests
        account = new Account(accountJson);
        databaseManager = new DatabaseManager(account);
        AccountManager accountManager = new AccountManager(account);

        //register/verify account and put some test videos/metadata into database
        String uuid = "456-sgdfgd3t5g-345fs";
        accountManager.registerAccount(uuid);
        account.setId(databaseManager.getAccountId());
        databaseManager.verifyAccount(uuid);
        databaseManager.saveProcessedVideoAndMeta("pod", "testMeta");
        databaseManager.saveProcessedVideoAndMeta("input2", "testMeta2");
        databaseManager.saveProcessedVideoAndMeta("input3", "metaTest");

        //setup for requests
        form = new Form();
        client = ClientBuilder.newClient();

        //set directories to TEST_RESOURCES_DIR
        try {
            setFinalStatic(LocationConfig.class.getDeclaredField("ANONYM_VID_DIR"), LocationConfig.TEST_RESOURCES_DIR);
            setFinalStatic(LocationConfig.class.getDeclaredField("META_DIR"), LocationConfig.TEST_RESOURCES_DIR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /* #############################################################################################
    *                                  valid tests
    * ###########################################################################################*/

    @Test
    public void authenticateValidTest() {
        form.param(ACCOUNT, accountJson);
        String responseString = authentication();
        if (responseString != null) {
            Assert.assertTrue(responseString.equals(SUCCESS));
        } else {
            Assert.fail();
        }
    }

    @Test
    public void verifyTest() {
        //setup for test
        Account tempAccount = new Account(tempAccountJson);
        DatabaseManager tempDatabaseManager = new DatabaseManager(tempAccount);
        AccountManager tempAccountManager = new AccountManager(tempAccount);
        tempAccountManager.registerAccount(tempUUID);
        tempAccount.setId(tempDatabaseManager.getAccountId());

        //client request (not using post method because of queryParameter)
        WebTarget webTarget = client.target(MAIN_ADDRESS).path("verifyAccount");
        Response response = webTarget.queryParam("uuid", tempUUID).request().get();
        Assert.assertTrue(response.readEntity(String.class).equals(SUCCESS));

        //cleanup
        tempDatabaseManager.deleteAccount();
    }

    @Test
    public void downloadTest() {
        //setup for test
        String videoId = Integer.toString(databaseManager.getVideoIdByName("pod"));
        File podAccount = new File(LocationConfig.TEST_RESOURCES_DIR + File.separator + account.getId() + "_pod"+ VideoInfo.FILE_EXTENTION);
        File podStandard = new File(LocationConfig.TEST_RESOURCES_DIR + File.separator + "pod" + VideoInfo.FILE_EXTENTION);
        Assert.assertTrue(podStandard.renameTo(podAccount));


        //client request
        form.param(ACCOUNT, accountJson);
        form.param("videoId", videoId);
        Response response = post("videoDownload");

        InputStream inputStream = null;
        if (response != null && response.getStatus() == 200) {
            inputStream = response.readEntity(InputStream.class);
            File downloadFile = new File(LocationConfig.TEST_RESOURCES_DIR + File.separator + "fileDownloadTest" + VideoInfo.FILE_EXTENTION);
            try {
                Files.copy(inputStream, downloadFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Assert.assertTrue(podAccount.renameTo(podStandard));
                boolean status = downloadFile.delete();
                Assert.assertTrue(status);
            } catch (IOException e) {
                Assert.fail();
            }
        } else {
            Assert.fail();
        }
    }

    @Test
    public void videosTest() {
        //setup for test
        form.param(ACCOUNT, accountJson);
        Response response = post("getVideos");
        JSONArray jsonArray = null;
        if (response != null) {
            jsonArray = new JSONArray(response.readEntity(String.class));
        } else {
            Assert.fail();
        }
        JSONObject jsonObject = jsonArray.getJSONObject(0);
        String jsonName = jsonObject.getString("name");
        Assert.assertTrue(jsonName.equals("pod"));
    }

    @Test
    public void createAccountTest() {
        //setup for test
        Account account2 = new Account(tempAccountJson);
        form.param(ACCOUNT, tempAccountJson);
        form.param("uuid", tempUUID);
        Response response = post("createAccount");
        DatabaseManager tempDM = new DatabaseManager(account2);
        account2.setId(tempDM.getAccountId());
        tempDM.deleteAccount();
        if (response != null) {
            Assert.assertTrue(response.readEntity(String.class).equals(SUCCESS));
        } else {
            Assert.fail();
        }
    }

    @Test
    public void changeAccountTest() {
        //setup for test
        form.param(ACCOUNT, accountJson);
        form.param("newAccount", tempAccountJson);
        Response response = post("changeAccount");
        if (response != null) {
            Assert.assertTrue(response.readEntity(String.class).equals(SUCCESS));
        } else {
            Assert.fail();
        }
    }

    @Test
    public void deleteAccountTest() {
        //setup for test
        Account tempAccount = new Account(tempAccountJson);
        DatabaseManager tempDatabaseManager = new DatabaseManager(tempAccount);
        AccountManager tempAccountManager = new AccountManager(tempAccount);
        tempAccountManager.registerAccount(tempUUID);
        tempAccount.setId(tempDatabaseManager.getAccountId());
        tempDatabaseManager.verifyAccount(tempUUID);
        tempDatabaseManager.saveProcessedVideoAndMeta("deleteVideo1", "deleteMeta1");
        tempDatabaseManager.saveProcessedVideoAndMeta("deleteVideo2", "deleteMeta2");

        //create files for testing
        File file1 = new File(LocationConfig.TEST_RESOURCES_DIR + File.separator + tempAccount.getId() + "_" + "deleteVideo1" + VideoInfo.FILE_EXTENTION);
        File file2 = new File(LocationConfig.TEST_RESOURCES_DIR + File.separator + tempAccount.getId() + "_" + "deleteVideo2" + VideoInfo.FILE_EXTENTION);
        File file3 = new File(LocationConfig.TEST_RESOURCES_DIR + File.separator + tempAccount.getId() + "_" + "deleteMeta1" + Metadata.FILE_EXTENTION);
        File file4 = new File(LocationConfig.TEST_RESOURCES_DIR + File.separator + tempAccount.getId() + "_" + "deleteMeta2" + Metadata.FILE_EXTENTION);
        try {
            file1.createNewFile();
            file2.createNewFile();
            file3.createNewFile();
            file4.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        form.param(ACCOUNT, tempAccountJson);
        Response response = post("deleteAccount");

        //cleanup
        tempDatabaseManager.deleteAccount();

        //various assertions
        Assert.assertTrue(response != null && response.readEntity(String.class).equals(SUCCESS));
        Assert.assertFalse(file1.exists());
        Assert.assertFalse(file2.exists());
        Assert.assertFalse(file3.exists());
        Assert.assertFalse(file4.exists());

    }

    @Test
    public void videoDeleteTest() {
        String videoId = "-1";
        String videoName = "input4";
        String metaName = "blaa";
        File video = new File(LocationConfig.TEST_RESOURCES_DIR + File.separator + account.getId() + "_" + videoName + VideoInfo.FILE_EXTENTION);
        File meta = new File(LocationConfig.TEST_RESOURCES_DIR + File.separator + account.getId() + "_" + metaName + Metadata.FILE_EXTENTION);
        boolean statusVideo = false;
        boolean statusMeta = false;
        try {
            statusVideo = video.createNewFile();
            statusMeta = meta.createNewFile();
        } catch (IOException e) {
            Assert.fail();
        }
        databaseManager.saveProcessedVideoAndMeta(videoName, metaName);
        ArrayList<VideoInfo> list = databaseManager.getVideoInfoList();
        for (VideoInfo videoInfo : list) {
            if (videoInfo.getName().equals("input4")) {
                videoId = Integer.toString(videoInfo.getVideoId());
            }
        }

        form.param(ACCOUNT, accountJson);
        form.param("videoId", videoId);
        Response response = post("videoDelete");
        databaseManager.deleteVideoAndMeta(databaseManager.getVideoIdByName("input4"));
        if (response != null && statusMeta && statusVideo) {
            Assert.assertTrue(response.readEntity(String.class).equals(SUCCESS));
        } else {
            Assert.fail();
        }
    }

    @Test
    public void videoInfoTest() {
        //setup
        String videoId = "-1";
        for (VideoInfo videoInfo : databaseManager.getVideoInfoList()) {
            if (videoInfo.getName().equals("input3")) {
                videoId = Integer.toString(videoInfo.getVideoId());
            }
        }
        Assert.assertFalse(videoId.equals("-1"));
        File metaAccount = new File(LocationConfig.TEST_RESOURCES_DIR +
                File.separator + account.getId() + "_" + "metaTest" + Metadata.FILE_EXTENTION);
        File metaStandard = new File (LocationConfig.TEST_RESOURCES_DIR +
                File.separator + "metaTest" + Metadata.FILE_EXTENTION);
        Assert.assertTrue(metaStandard.renameTo(metaAccount));

        //client request
        form.param(ACCOUNT, accountJson);
        form.param("videoId", videoId);
        Response response = post("videoInfo");
        String entity = null;
        if (response != null) {
            entity = response.readEntity(String.class);
            Assert.assertTrue(metaAccount.renameTo(metaStandard));
        } else {
            Assert.fail();
        }
        if (!entity.equals("FAILURE")) {
            JSONObject jsonObject = new JSONObject(entity);
            float gForceY = (float) jsonObject.getDouble("triggerForceY");
            Assert.assertTrue(gForceY == 40.0f);
        } else {
            Assert.fail();
        }


    }

    @Test
    public void uploadValidTest() {
        //set directories to standard
        try {
            setFinalStatic(LocationConfig.class.getDeclaredField("ANONYM_VID_DIR"), anonym_dir);
            setFinalStatic(LocationConfig.class.getDeclaredField("META_DIR"), meta_dir);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //client request (here using multipart feature for upload)
        MultiPart multiPart = new MultiPart();
        multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);
        FileDataBodyPart video = new FileDataBodyPart("video", new File(LocationConfig.TEST_RESOURCES_DIR + File.separator + "VIDEO_1487198226374" + VideoInfo.FILE_EXTENTION), MediaType.APPLICATION_OCTET_STREAM_TYPE);
        FileDataBodyPart metadata = new FileDataBodyPart("metadata", new File(LocationConfig.TEST_RESOURCES_DIR + File.separator + "META_1487198226374" + Metadata.FILE_EXTENTION), MediaType.APPLICATION_OCTET_STREAM_TYPE);
        FileDataBodyPart key = new FileDataBodyPart("key", new File(LocationConfig.TEST_RESOURCES_DIR + File.separator + "KEY_1487198226374.key"), MediaType.APPLICATION_OCTET_STREAM_TYPE);
        FormDataBodyPart data = new FormDataBodyPart(ACCOUNT, accountJson);
        multiPart.bodyPart(video);
        multiPart.bodyPart(metadata);
        multiPart.bodyPart(key);
        multiPart.bodyPart(data);
        String responseString = upload(multiPart);
        if (responseString != null) {
            Assert.assertTrue(responseString.equals("Finished editing video"));
        } else {
            Assert.fail();
        }

        //cleanup
        databaseManager.deleteVideoAndMeta(databaseManager.getVideoIdByName("VIDEO_1487198226374"));
        File encVid = new File(LocationConfig.ANONYM_VID_DIR + File.separator + account.getId() + "_VIDEO_1487198226374" + VideoInfo.FILE_EXTENTION);
        File encMeta = new File(LocationConfig.META_DIR + File.separator + account.getId() + "_VIDEO_1487198226374_meta" + Metadata.FILE_EXTENTION);
        boolean status = encVid.delete();
        Assert.assertTrue(status);
        status = encMeta.delete();
        Assert.assertTrue(status);
    }

    /* #############################################################################################
    *                                   fail tests
    * ###########################################################################################*/

    @Test
    public void authenticateFailTest() {
        String responseString = authentication();
        if (responseString != null) {
            Assert.assertTrue(responseString.equals(FAILURE));
        } else {
            Assert.fail();
        }
    }

    @Test
    public void uploadFailTest() {
        //set directories to standard
        try {
            setFinalStatic(LocationConfig.class.getDeclaredField("ANONYM_VID_DIR"), anonym_dir);
            setFinalStatic(LocationConfig.class.getDeclaredField("META_DIR"), meta_dir);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //client request
        MultiPart multiPart = new MultiPart();
        multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);
        FormDataBodyPart data = new FormDataBodyPart(ACCOUNT, accountJson);
        multiPart.bodyPart(data);
        String responseString = upload(multiPart);
        if (responseString != null) {
            Assert.assertTrue(responseString.equals("Uploaded data was not received correctly"));
        } else {
            Assert.fail();
        }
    }

    /* #############################################################################################
    *                                   responses
    * ###########################################################################################*/

    private String authentication () {
        //form must be filled in calling function
        Response response = post("authenticate");
        if (response != null) {
            return response.readEntity(String.class);
        }
        return null;
    }

    private String upload(MultiPart multiPart) {
        WebTarget webTarget = client.target(MAIN_ADDRESS).path("videoUpload").register(MultiPartFeature.class);
        Future<Response> futureResponse = webTarget.request().async().post(Entity.entity(multiPart, multiPart.getMediaType()), Response.class);
        try {
            Response response = futureResponse.get();
            return response.readEntity(String.class);
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }


    private Response post(String path) {
        WebTarget webTarget = client.target(MAIN_ADDRESS).path(path);
        try {
            return webTarget.request().post(
                    Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), Response.class);
        } catch (ProcessingException e) {
            return null;
        }
    }

    @After
    public void after() {
        //set directories back to original paths
        try {
            setFinalStatic(LocationConfig.class.getDeclaredField("ANONYM_VID_DIR"), anonym_dir);
            setFinalStatic(LocationConfig.class.getDeclaredField("META_DIR"), meta_dir);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //delete videos/metadata/account from database
        databaseManager.deleteVideoAndMeta(databaseManager.getVideoIdByName("pod"));
        databaseManager.deleteVideoAndMeta(databaseManager.getVideoIdByName("input2"));
        databaseManager.deleteVideoAndMeta(databaseManager.getVideoIdByName("input3"));
        databaseManager.deleteAccount();

        //stop server
        Main.stopServer();
    }
}

