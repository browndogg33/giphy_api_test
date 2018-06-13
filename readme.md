# Giphy Test Project

## Summary
Project created to run some sample API tests against the [Giphy](http://www.giphy.com) API.  Initial project tests getting
a gif by id.  

## Assignment
* Create a suite of tests that are similar to the existing tests but for the individual sticker pack endpoint 
on Giphy - https://developers.giphy.com/docs/#sticker-packs-individual-endpoint.  Just do some basic assertions like the existing
test suite for getting a gif by id.
* Create a test with basic assertions that gets the gifs related to the term "basketball", limit the results to 5, and limit the 
rating of the results to "g"
* This should take no longer than an hour or so

## Prerequisites
* Java installed
* Git installed
* Maven installed

## Command to kick off the test suite
Run this inside the project root
<code>
mvn test
</code>

## Reference Documentation
* [Rest Assured](https://github.com/rest-assured/rest-assured/wiki/Usage)
* [Giphy API Docs](https://developers.giphy.com/docs/) Testing Individual Sticker Pack Endpoint


