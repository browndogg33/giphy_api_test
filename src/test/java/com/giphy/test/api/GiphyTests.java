package com.giphy.test.api;


import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.giphy.test.category.ApiTests;

import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(ApiTests.class)
public class GiphyTests extends GiphyTestBase {

    //GIF Tests
    @Test
    public void getGifByIdReturnsGif(){
        given().
                param("api_key", API_KEY).
        when().
                get("/gifs/zdIGTIdD1mi4").
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
                get("/gifs/zdIGTIdD1mi4").
        then().
                statusCode(401)
                .body("message", equalTo("No API key found in request"));
    }

    @Test
    public void getGifByIdBadAPIKey(){
        given().
                param("api_key", BAD_API_KEY).
        when().
                get("/gifs/zdIGTIdD1mi4").
        then().
                statusCode(403)
                .body("message", equalTo("Invalid authentication credentials"));
    }

    @Test
    public void getGifByIdNotFound(){
        given().
                param("api_key", API_KEY).
        when().
                get("/gifs/zdIGTIdD1mi4XXXX").
        then().
                statusCode(404)
                .body("meta.msg", equalTo("Not Found"));
    }
}
