package com.giphy.test.api;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.List;

import com.giphy.test.category.ApiTests;

import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import io.restassured.response.Response;

@Category(ApiTests.class)
public class StickerSearchTests extends GiphyTestBase {

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
    public void stickerSearchLimitNegative() {
        final Response outOfBoundsResponse = given()
                .param("api_key", API_KEY)
                .param("limit", -10)
        .when()
                .get("/stickers/search");

        assertValidResponse(outOfBoundsResponse)
                .body("data", hasSize(0))
                .body("pagination.total_count", is(0))
                .body("pagination.count", is(0))
                .body("pagination.offset", is(0));
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

    @Test
    public void stickerSearchOffsetNegative() {
        final Response outOfBoundsResponse = given()
                .param("api_key", API_KEY)
                .param("offset", -10)
        .when()
                .get("/stickers/search");

        assertValidResponse(outOfBoundsResponse)
                .body("data", hasSize(0))
                .body("pagination.total_count", is(0))
                .body("pagination.count", is(0))
                .body("pagination.offset", is(0));
    }

    @Test
    public void stickerSearchOffsetHigherThanTotalCount() {
        final String searchTerm = "unique";
        final Response countResponse = given()
                .param("api_key", API_KEY)
                .param("q", searchTerm)
        .when()
                .get("/stickers/search");
        assertValidResponse(countResponse);
        final int totalCount = countResponse.path("pagination.total_count");

        final Response outOfBoundsResponse = given()
                .param("api_key", API_KEY)
                .param("q", searchTerm)
                .param("offset", totalCount + 10)
        .when()
                .get("/stickers/search");
        
        //Interesting that this differs from response of gifTrendingOffsetHigherThanTotalCount()
        assertValidResponse(outOfBoundsResponse)
                .body("data", hasSize(0))
                .body("pagination.total_count", is(totalCount))
                .body("pagination.count", is(0))
                .body("pagination.offset", is(totalCount + 10));
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
    
}
