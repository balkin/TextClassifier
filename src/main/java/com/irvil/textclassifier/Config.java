package com.irvil.textclassifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;


// todo: add default configuration and create config file automatically
@Slf4j
public class Config {

    private final Properties properties = new Properties();

    public Config(String fileName) {
        try (InputStream inputStream = new FileInputStream(fileName)) {
            properties.load(inputStream);
        } catch (IOException e) {
            log.error("Failed to load config file {}", fileName);
        }
    }

    public boolean isLoaded() {
        return properties.size() > 0;
    }

    public String getDbPath() {
        return System.getProperty("user.dir") + "/../TextClassifier/db"; //  getProperty("db_path");
    }

    public String getDaoType() {
        return getProperty("dao_type");
    }

    public String getDBMSType() {
        return getProperty("dbms_type");
    }

    public String getDbFileName() {
        return getProperty("db_filename");
    }

    public String getNGramStrategy() {
        return getProperty("ngram_strategy");
    }

    private String getProperty(String property) {
        return properties.getProperty(property) != null ? properties.getProperty(property) : "";
    }
}