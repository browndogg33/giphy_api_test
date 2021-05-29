package com.giphy.test.api;


import com.giphy.test.category.ApiTests;

import org.hamcrest.collection.IsEmptyCollection;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.boot.test.context.SpringBootTest;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Category(ApiTests.class)
@SpringBootTest
public class GiphyTests {

    private static String API_KEY = Props.retrieveApiKey();;
    private static final String BAD_API_KEY = "XXXXXXXXXXXXXXXXXXXXXXXXXXXX";

    @BeforeClass 
    public static void setup() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.baseURI  ="http://api.giphy.com/v1";
    }

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

    //Sticker Search Tests
    @Test
    public void stickerSearchNoApiKey() {
        when()
                .get("/stickers/search")
        .then()
                .statusCode(401)
                .body("message", equalTo("No API key found in request"));
    }

    @Test
    public void stickerSearchInvalidApiKey() {
        given()
                .param("api_key", BAD_API_KEY)
        .when()
                .get("/stickers/search")
        .then()
                .statusCode(403)
                .body("message", equalTo("Invalid authentication credentials")); 
    }

    @Test
    public void stickerSearchEmptySearchTerm() {
        given()
                .param("api_key", API_KEY)
        .when()
                .get("/stickers/search")
        .then()
                .statusCode(200)
                .body("meta.msg", is("OK"))
                .body("data", IsEmptyCollection.empty())
                .body("pagination.count", is(0))
                .body("pagination.total_count", is(0)); 
    }

    private ValidatableResponse assertValidResponse(final Response response) {
        return response.then()
                .statusCode(200)
                .body("meta.status", is(200))
                .body("meta.msg", is("OK"))
                .body("meta.response_id", is(notNullValue(String.class))); 
    }

    @Test
    public void stickerSearchLimit() {
        final Response response = given()
                .param("api_key", API_KEY)
                .param("q", "cheeseburgers")
                .param("limit", 5)
        .when()
                .get("/stickers/search");

        assertValidResponse(response)
                .body("data", hasSize(5))
                .body("pagination.total_count", is(greaterThanOrEqualTo(5)));
    }

    @Test
    public void stickerSearchLimitBetaApiKeyLimit() {
        //Check that Beta API key limits to 50 no matter what is requested
        final Response response = given()
                .param("api_key", API_KEY)
                .param("q", "cheeseburgers")
                .param("limit", "100") 
        .when()
                .get("/stickers/search"); 

        assertValidResponse(response)
                .body("data", hasSize(50))
                .body("pagination.total_count", is(greaterThanOrEqualTo(50)));
    }

    private List<String> getExpectedOffsetIds(final String searchTerm) {
        final Response response = given()
                .param("api_key", API_KEY)
                .param("q", searchTerm)
                .param("limit", "10") 
        .when()
                .get("/stickers/search");

        assertValidResponse(response);
        return response.path("data.collect { it.id }");
    }

    @Test
    public void stickerSearchOffset() {
        final String searchTerm = "cheeseburgers";
        final List<String> expectedList = getExpectedOffsetIds(searchTerm);

        for(int index = 0; index < expectedList.size(); ++index) {
            final String expectedId = expectedList.get(index);

            final Response response = given()
                    .param("api_key", API_KEY)
                    .param("q", searchTerm)
                    .param("limit", "1")
                    .param("offset", String.valueOf(index))
            .when()
                    .get("/stickers/search");

                assertValidResponse(response)
                    .body("data", hasSize(1))
                    .body("data[0].id", is(expectedId));
        }
    }

    private final static List<String> acceptableRatings = Arrays.asList("g", "pg","pg-13", "r");

    //rating can be up to requested rating, so we build up our filter
    private String buildRatingsFilter(final int toIndex) {
        return acceptableRatings.subList(0, toIndex)
                .stream()
                .map(currentRating -> "it.rating != '" + currentRating + "'")
                .collect(Collectors.joining(" && "));
    }

    @Test
    public void stickerSearchRatings() {  
        for(int index = 0; index < acceptableRatings.size(); ++index) {
                final String rating = acceptableRatings.get(index);
                final Response response = given()
                        .param("api_key", API_KEY)
                        .param("q", "death")
                        .param("rating", rating)
                .when()
                        .get("/stickers/search");

                assertValidResponse(response)
                        .body("data.findAll { " + buildRatingsFilter(index + 1) + " }", IsEmptyCollection.empty());
        }
    }

    //Trending GIF Tests
    @Test
    public void gifTrendingNoApiKey() {
        when()
                .get("/gif/trending")
        .then()
                .statusCode(401)
                .body("message", equalTo("No API key found in request"));
    }

    @Test
    public void gifTrendingInvalidApiKey() {
        given()
                .param("api_key", BAD_API_KEY)
        .when()
                .get("/gifs/trending")
        .then()
                .statusCode(403)
                .body("message", equalTo("Invalid authentication credentials")); 
    }

    @Ignore("Their docs say that the default of limit is 25, but if you leave it off, you get 50")
    @Test
    public void gifTrendingDefaults() {
        final Response response = given()
                .param("api_key", API_KEY)
        .when()
                .get("/gifs/trending");

        assertValidResponse(response)
                .body("data", hasSize(25))
                .body("pagination.total_count", is(greaterThanOrEqualTo(25)))
                .body("pagination.count", is(25))
                .body("pagination.offset", is(0));
    }

    @Test
    public void gifTrendingLimit() {
        final Response response = given()
                .param("api_key", API_KEY)
                .param("q", "cheeseburgers")
                .param("limit", 5)
        .when()
                .get("/gifs/trending");

        assertValidResponse(response)
                .body("data", hasSize(5))
                .body("pagination.total_count", is(greaterThanOrEqualTo(5)))
                .body("pagination.count", is(5));
    }

    @Test
    public void gifTrendingLimitBetaApiKeyLimit() {
        //Check that Beta API key limits to 50 no matter what is requested
        final Response response = given()
                .param("api_key", API_KEY)
                .param("q", "cheeseburgers")
                .param("limit", "100") 
        .when()
                .get("/gifs/trending");

        assertValidResponse(response)
                .body("data", hasSize(50))
                .body("pagination.total_count", is(greaterThanOrEqualTo(50)))
                .body("pagination.count", is(50));
    }

    private List<String> getExpectedOffsetIdsForTrending() {
        final Response response = given()
                .param("api_key", API_KEY)
                .param("limit", "10") 
        .when()
                .get("/gifs/trending");

        assertValidResponse(response);
        System.out.println((List<String>)response.path("data.collect { it.id }") );
        return response.path("data.collect { it.id }");
    }

    //TODO: need to figure out why this test is failing
    @Test
    public void gifTrendingOffset() {
        final List<String> expectedList = getExpectedOffsetIdsForTrending();

        for(int index = 0; index < expectedList.size(); ++index) {
                System.out.println(index);
            final String expectedId = expectedList.get(index);
            System.out.println("expected id = " + expectedId);

            final Response response = given()
                    .param("api_key", API_KEY)
                    .param("limit", "1")
                    .param("offset", String.valueOf(index))
            .when()
                .get("/gifs/trending");

                assertValidResponse(response)
                    .body("data", hasSize(1))
                    .body("data[0].id", is(expectedId));
        }
    }

    @Test
    public void gifTrendingRatings() {  
        for(int index = 0; index < acceptableRatings.size(); ++index) {
                final String rating = acceptableRatings.get(index);
                final Response response = given()
                        .param("api_key", API_KEY)
                        .param("rating", rating)
                .when()
                        .get("/gifs/trending");

                assertValidResponse(response)
                        .body("data.findAll { " + buildRatingsFilter(index + 1) + " }", IsEmptyCollection.empty());
        }
    }
}
