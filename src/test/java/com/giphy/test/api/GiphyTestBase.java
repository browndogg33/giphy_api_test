package com.giphy.test.api;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.giphy.test.Props;

import org.junit.BeforeClass;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;

public class GiphyTestBase {

    protected static final String API_KEY = Props.retrieveApiKey();;
    protected static final String BAD_API_KEY = "XXXXXXXXXXXXXXXXXXXXXXXXXXXX";

    @BeforeClass 
    public static void setup() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.baseURI  ="http://api.giphy.com/v1";
    }

    protected ValidatableResponse assertValidResponse(final Response response) {
        return response.then()
                .statusCode(200)
                .body("meta.status", is(200))
                .body("meta.msg", is("OK"))
                .body("meta.response_id", is(notNullValue(String.class))); 
    }

    protected final static List<String> acceptableRatings = Arrays.asList("g", "pg","pg-13", "r");

    //rating can be up to requested rating, so we build up our filter
    protected String buildRatingsFilter(final int toIndex) {
        return acceptableRatings.subList(0, toIndex)
                .stream()
                .map(currentRating -> "it.rating != '" + currentRating + "'")
                .collect(Collectors.joining(" && "));
    }
    
}
