package com.irvil.textclassifier.dao.jpa;

import com.irvil.textclassifier.dao.StorageCreator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class HibernateDBCreator implements StorageCreator {
  public Connection getConnection() throws SQLException {
    try {
      Class.forName("org.h2.Driver");
    } catch (ClassNotFoundException ignored) {
    }

    return DriverManager.getConnection("jdbc:h2:tcp://localhost/C:\\Users\\r.zakaryaev\\_data\\projects\\TextClassifier\\db\\hibernate_db;");
  }

  @Override
  public void createStorage() {
    List<String> sqlQueries = new ArrayList<>();

    // create database structure
    //

//    sqlQueries.add("CREATE TABLE IF NOT EXISTS CharacteristicsNames " +
//        "(Id INT AUTO_INCREMENT PRIMARY KEY, Name CLOB )");
//    sqlQueries.add("CREATE TABLE IF NOT EXISTS CharacteristicsValues " +
//        "(Id INT, CharacteristicsNameId INT, Value CLOB, PRIMARY KEY(Id, CharacteristicsNameId))");
//    sqlQueries.add("CREATE TABLE IF NOT EXISTS ClassifiableTexts " +
//        "(Id INT AUTO_INCREMENT PRIMARY KEY, Text CLOB)");
//    sqlQueries.add("CREATE TABLE IF NOT EXISTS ClassifiableTextsCharacteristics " +
//        "(ClassifiableTextId INT, CharacteristicsNameId INT, CharacteristicsValueId INT, PRIMARY KEY(ClassifiableTextId, CharacteristicsNameId, CharacteristicsValueId))");
//    sqlQueries.add("CREATE TABLE IF NOT EXISTS Vocabulary " +
//        "(Id INT AUTO_INCREMENT PRIMARY KEY, Value CLOB)");

    // clear all tables
    //

    sqlQueries.add("DELETE FROM Vocabulary");
    sqlQueries.add("DELETE FROM ClassifiableTextsCharacteristics");
    sqlQueries.add("DELETE FROM ClassifiableTexts");
    sqlQueries.add("DELETE FROM CharacteristicsValues");
    sqlQueries.add("DELETE FROM CharacteristicsNames");

    // reset all autoincrement keys
    //

    sqlQueries.add("ALTER TABLE CharacteristicsNames ALTER COLUMN Id RESTART WITH 1");
    sqlQueries.add("ALTER TABLE CharacteristicsValues ALTER COLUMN Id RESTART WITH 1");
    sqlQueries.add("ALTER TABLE ClassifiableTexts ALTER COLUMN Id RESTART WITH 1");
    sqlQueries.add("ALTER TABLE Vocabulary ALTER COLUMN Id RESTART WITH 1");

    // execute queries
    //

    try (Connection con = getConnection()) {
      Statement statement = con.createStatement();

      for (String sqlQuery : sqlQueries) {
        try {
          statement.execute(sqlQuery);
        } catch (SQLException ignored) {
          ignored.printStackTrace();
        }
      }
    } catch (SQLException ignored) {
    }
  }
}