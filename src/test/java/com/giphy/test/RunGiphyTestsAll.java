package com.giphy.test;

import com.giphy.test.api.GifTrendingTests;
import com.giphy.test.api.GiphyTests;
import com.giphy.test.api.StickerSearchTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        GiphyTests.class, 
        StickerSearchTests.class, 
        GifTrendingTests.class
})
public class RunGiphyTestsAll {
}
