package org.demo.tests;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.demo.models.TokenGeneratorResponse;
import org.demo.models.UserData;
import org.demo.models.UserRequest;
import org.demo.models.UserResponse;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Objects;

import static io.restassured.RestAssured.given;

public class DemoTest {

    private static final String addUserPath = "/Account/v1/User";
    private static final String authorizedPath = "/Account/v1/Authorized";
    private static final String tokenGeneratorPath = "/Account/v1/GenerateToken";

    // negative data path
    private static final String negativeDataPath = "/data/user-negative-test-data.json";

    private static final UserRequest mUser = new UserRequest("User" + System.currentTimeMillis(), "Automation@!@123");


    @BeforeClass
    public void init() {
        RestAssured.baseURI = "https://bookstore.toolsqa.com";
    }

    @Test(priority = 1, dataProvider = "user-negative-data-provider")
    public void addUser_negativeTest(String name) {
        var userData = getUserDataByName(name);

        var userResponse = given().contentType(ContentType.JSON)
                .body(userData.request())
                .when().post(addUserPath)
                .then()
                .extract().body().as(userData.response().getClass());

        Assert.assertEquals(userResponse, userData.response());
    }


    @Test(priority = 2)
    public void addUserAndAuthorize_positiveTest() {
        var userResponse = given().contentType(ContentType.JSON)
                .body(mUser)
                .when().post(addUserPath)
                .then().extract().body().as(UserResponse.class);

        Assert.assertTrue(userResponse.books().isEmpty()
                , "Book list must be empty");

        Assert.assertEquals(userResponse.username(), mUser.userName()
                , "username is different than expected");


        Assert.assertFalse(isAuthorized()
                , "newly added user must not be authorized");

        var tokenResponse = given().contentType(ContentType.JSON)
                .body(mUser)
                .when().post(tokenGeneratorPath)
                .then()
                .statusCode(200)
                .extract().body().as(TokenGeneratorResponse.class);

        Assert.assertNotNull(tokenResponse);
        Assert.assertEquals(tokenResponse.status(), "Success");
        Assert.assertNotNull(tokenResponse.token());
        Assert.assertNotNull(tokenResponse.expires());
        Assert.assertEquals(tokenResponse.result(), "User authorized successfully.");

        Assert.assertTrue(isAuthorized()
                , "user must be authorized after successful generated token");

    }


    @DataProvider(name = "user-negative-data-provider")
    public Object[][] negativeDataProvider() {
        return getUserData()
                .stream()
                .map(it -> new String[]{it.name()})
                .toArray(Object[][]::new);
    }

    private boolean isAuthorized() {
        return given().contentType(ContentType.JSON)
                .body(mUser)
                .when().post(authorizedPath)
                .then()
                .statusCode(200)
                .extract().body().as(Boolean.class);
    }

    public UserData getUserDataByName(String name) {
        return getUserData().stream().filter(it -> it.name().equals(name))
                .findAny()
                .orElseThrow();
    }

    private List<UserData> getUserData() {
        try {
            var path = Objects.requireNonNull(getClass().getResource(negativeDataPath)).getPath();
            return new Gson().fromJson(new FileReader(path), new TypeToken<>() {
            });
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
