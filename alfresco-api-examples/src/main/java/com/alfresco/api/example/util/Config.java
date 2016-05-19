package com.alfresco.api.example.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {

    private static Properties config;

    public static Properties getConfig() {
        if (config == null) {
            config = new Properties();
            try {
                   config.load(new FileInputStream("config.properties"));
               } catch (IOException ioe) {
                   ioe.printStackTrace();
            }

        }
        return config;
    }

}
