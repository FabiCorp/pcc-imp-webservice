package edu.kit.informatik.pcc.service.manager;

import edu.kit.informatik.pcc.service.data.Account;
import edu.kit.informatik.pcc.service.data.DatabaseManager;
import org.bytedeco.javacpp.presets.opencv_core;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Base64;

/**
 * @author Fabian Wenzel
 *         Created by Fabian Wenzel on 12.02.2017.
 */
public class AccountManagerTest {
    // status message constants
    private final String SUCCESS = "SUCCESS";
    private final String FAILURE = "FAILURE";

    private String accountJson;
    private Account account;
    private DatabaseManager databaseManager;
    private AccountManager accountManager;

    private String uuid = "456-sgdfgd3t5g-345fs";
    private byte[] registerSalt = new byte[16];

    //TODO: test password hashing
    //TODO: test e-mail validation
    @Before
    public void setUp() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mail", "fabiistkrass@gmail.de");
        jsonObject.put("password", "yochilldeinlife");
        accountJson = jsonObject.toString();

        //setup account (registered)
        account = new Account(accountJson);
        accountManager = new AccountManager(account);
        databaseManager = new DatabaseManager(account);
    }

    private void registerTestAccount(){
        account.hashPassword(registerSalt);
        String saltString = Base64.getEncoder().encodeToString(registerSalt);
        databaseManager.register(uuid, saltString);
        account.setId(databaseManager.getAccountId());
    }

    @Test
    public void createSaltTest() {
        registerTestAccount();
        String originalSaltString = Base64.getEncoder().encodeToString(registerSalt);
        byte[] salt = accountManager.getSalt();
        String saltString = Base64.getEncoder().encodeToString(salt);
        Assert.assertTrue(saltString.equals(originalSaltString));
    }

    @Test
    public void changeTest() {
        //setup
        registerTestAccount();
        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("mail", "fabiistababa@yahoo.com");
        jsonObject2.put("password", "ichbindershitfuckyooo");
        String tempAccountJson = jsonObject2.toString();

        String status = accountManager.changeAccount(tempAccountJson);
        Assert.assertTrue(status.equals(SUCCESS));
    }

    @Test
    public void registerTest() {
        String status = accountManager.registerAccount(uuid);
        account.setId(databaseManager.getAccountId());
        Assert.assertTrue(status.equals(SUCCESS));
    }

    @Test
    public void deleteTest() {
        registerTestAccount();
        VideoManager videoManager = new VideoManager(account);
        String status = accountManager.deleteAccount(videoManager);
        Assert.assertTrue(status.equals(SUCCESS));
    }
    @Test
    public void accountIdTest() {
        registerTestAccount();
        int id = accountManager.getAccountId();
        Assert.assertEquals(id, account.getId());
    }

    @Test
    public void authenticateTest() {
        registerTestAccount();
        boolean status = accountManager.authenticate();
        Assert.assertTrue(status);
    }
    @Test
    public void verifyTest() {
        registerTestAccount();
        String status = accountManager.verifyAccount(uuid);
        Assert.assertTrue(status.equals(SUCCESS));
    }

    @Test
    public void isVerifiedTest() {
        registerTestAccount();
        databaseManager.verifyAccount(uuid);
        boolean verified = accountManager.isVerified();
        Assert.assertTrue(verified);
    }


    @After
    public void cleanUp() {
        databaseManager.deleteAccount();
    }
}
