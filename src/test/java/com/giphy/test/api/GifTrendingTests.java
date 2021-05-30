package com.giphy.test.api;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashSet;
import java.util.Set;

import com.giphy.test.category.ApiTests;

import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import io.restassured.response.Response;

@Category(ApiTests.class)
public class GifTrendingTests extends GiphyTestBase {

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

    @Test
    public void gifTrendingLimitNegative() {
        final Response outOfBoundsResponse = given()
                .param("api_key", API_KEY)
                .param("limit", -10)
        .when()
                .get("/gifs/trending");   

        assertValidResponse(outOfBoundsResponse)
                .body("data", hasSize(0))
                .body("pagination.total_count", is(0))
                .body("pagination.count", is(0))
                .body("pagination.offset", is(0));
    }

    @Test
    public void gifTrendingOffset() {
        //Paging seems to be pretty flakey when you're trying to get the very next results
        //So this test actually offsets to 40 to be sure its in new trending gif territory
        
        final Set<String> uniqueIds = new HashSet<>();

        final Response firstPage = given()
                    .param("api_key", API_KEY)
                    .param("limit", "10")
                    .param("offset", 0)
            .when()
                .get("/gifs/trending");
        assertValidResponse(firstPage)
                .body("data", hasSize(10))
                .body("pagination.count", is(10))
                .body("pagination.offset", is(0));
        uniqueIds.addAll(firstPage.path("data.collect { it.id }"));

        final Response secondPage = given()
                    .param("api_key", API_KEY)
                    .param("limit", "10")
                    .param("offset", 40)
            .when()
                .get("/gifs/trending");
        assertValidResponse(secondPage)
                .body("data", hasSize(10))
                .body("pagination.count", is(10))
                .body("pagination.offset", is(40));
        uniqueIds.addAll(secondPage.path("data.collect { it.id }"));

        assertThat(uniqueIds, hasSize(20));
    }

    @Test
    public void gifTrendingOffsetNegative() {
        final Response outOfBoundsResponse = given()
                .param("api_key", API_KEY)
                .param("offset", -10)
        .when()
                .get("/gifs/trending");   

        assertValidResponse(outOfBoundsResponse)
                .body("data", hasSize(0))
                .body("pagination.total_count", is(0))
                .body("pagination.count", is(0))
                .body("pagination.offset", is(0));
    }

    @Test
    public void gifTrendingOffsetHigherThanTotalCount() {
        final Response countResponse = given()
                .param("api_key", API_KEY)
        .when()
                .get("/gifs/trending");   
        assertValidResponse(countResponse);
        final int totalCount = countResponse.path("pagination.total_count");

        final Response outOfBoundsResponse = given()
                .param("api_key", API_KEY)
                .param("offset", totalCount + 10)
        .when()
                .get("/gifs/trending");   
        
        //Interesting that this differs from response of stickerSearchOffsetHigherThanTotalCount()
        assertValidResponse(outOfBoundsResponse)
                .body("data", hasSize(0))
                .body("pagination.total_count", is(0))
                .body("pagination.count", is(0))
                .body("pagination.offset", is(0));
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
