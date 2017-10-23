package com.irvil.textclassifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

// todo: add default configuration and create config file automatically
public class Config {
  private final Properties properties = new Properties();

  public Config(String fileName) {
    // read config file
//	  URL resource = this.getClass().getResource("/");
//	  File file = new File (fileName);
//	  file.exists();
//    try (InputStream inputStream = Config.class.getResourceAsStream(fileName)) {
//      properties.load(inputStream);
//    } catch (IOException ignored) {
//
//    }
  }
		  
  public boolean isLoaded() {
    return true; //properties.size() > 0;
  }

  public String getDbPath() {
    return System.getProperty("user.dir") + "/../TextClassifier/db"; //  getProperty("db_path");
  }

  public String getDaoType() {
    return "jdbc"; //getProperty("dao_type");
  }

  public String getDBMSType() {
    return "h2"; //getProperty("dbms_type");
  }

  public String getDbFileName() {
    return "TextClassifier"; //getProperty("db_filename");
  }

  public String getNGramStrategy() {
    return "filtered_unigram"; //getProperty("ngram_strategy");
  }

  private String getProperty(String property) {
    return properties.getProperty(property) != null ? properties.getProperty(property) : "";
  }
}