package com.giphy.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class Props {

    public static String retrieveApiKey() {
        try (InputStream input = new FileInputStream("test.properties")) {
            Properties prop = new Properties();
            prop.load(input);

            final String apiKey = prop.getProperty("api_key");
            if(apiKey == null) {
                throw new RuntimeException("API Key cannot be null!");
            }
            
            return apiKey;
        } catch (final IOException ex) {
            //clean up and throw meaningful response
            throw new RuntimeException(ex);
        }
    }
    
}
