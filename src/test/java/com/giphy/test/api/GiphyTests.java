package com.giphy.test.api;


import com.giphy.test.category.ApiTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.boot.test.context.SpringBootTest;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;

@Category(ApiTests.class)
@SpringBootTest
public class GiphyTests {

    private static final String API_KEY = "gpUyROOE6QF2GlJD2Jy59gIuX6mNpb7q";
    private static final String BAD_API_KEY = "XXXXXXXXXXXXXXXXXXXXXXXXXXXX";

    //GIF Tests
    @Test
    public void getGifByIdReturnsGif(){
        given().
                param("api_key", API_KEY).
        when().
                get("http://api.giphy.com/v1/gifs/zdIGTIdD1mi4").
        then().
                statusCode(200)
                .body("data.type", equalTo("gif"))
                .body("data.id", is("zdIGTIdD1mi4"))
                .body("data.rating", is("g"))
                .body("meta.msg", is("OK"));
    }

    @Test
    public void getGifByIdNoAPIKey(){
        when().
                get("http://api.giphy.com/v1/gifs/zdIGTIdD1mi4").
        then().
                statusCode(401)
                .body("meta.status", equalTo(401))
                .body("meta.msg", equalTo("No API key found in request."))
                .body("meta.response_id", isEmptyString());
    }

    @Test
    public void getGifByIdBadAPIKey(){
        given().
                param("api_key", BAD_API_KEY).
        when().
                get("http://api.giphy.com/v1/gifs/zdIGTIdD1mi4").
        then().
                statusCode(401)
                .body("meta.status", equalTo(401))
                .body("meta.msg", equalTo("Unauthorized"))
                .body("meta.response_id", isEmptyString());
    }

    @Test
    public void getGifByIdNotFound(){
        given().
                param("api_key", API_KEY).
        when().
                get("http://api.giphy.com/v1/gifs/zdIGTIdD1mi4XXXX").
        then().
                statusCode(404)
                .body("meta.msg", equalTo("Not Found"))
                .body("meta.status", equalTo(404))
                .body("meta.error_code", equalTo(404));
    }

    //TODO
    //Sticker Search test code goes here

}
