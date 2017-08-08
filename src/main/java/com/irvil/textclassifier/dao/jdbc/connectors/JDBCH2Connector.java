package com.irvil.textclassifier.dao.jdbc.connectors;

import org.h2.tools.DeleteDbFiles;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JDBCH2Connector implements JDBCConnector {
  private final String dbPath;
  private final String dbFileName;
  private final String dbName;

  public JDBCH2Connector(String dbPath, String dbFileName) {
    if (dbFileName == null || dbFileName.equals("")) {
      throw new IllegalArgumentException();
    }

    this.dbPath = dbPath;
    this.dbFileName = dbFileName;
    this.dbName = dbPath + "/" + dbFileName;
  }

  @Override
  public Connection getConnection() throws SQLException {
    try {
      Class.forName("org.h2.Driver");
    } catch (ClassNotFoundException ignored) {
    }

    return DriverManager.getConnection("jdbc:h2:" + dbName);
  }

  @Override
  public void createStorage() {
    // delete old database file
    DeleteDbFiles.execute(dbPath, dbFileName, true);

    List<String> sqlQueries = new ArrayList<>();

    // create database structure
    //

    sqlQueries.add("CREATE TABLE CharacteristicsNames " +
        "(Id INT AUTO_INCREMENT PRIMARY KEY, Name CLOB )");
    sqlQueries.add("CREATE TABLE CharacteristicsValues " +
        "(Id INT, CharacteristicsNameId INT, Value CLOB, PRIMARY KEY(Id, CharacteristicsNameId))");
    sqlQueries.add("CREATE TABLE ClassifiableTexts " +
        "(Id INT AUTO_INCREMENT PRIMARY KEY, Text CLOB)");
    sqlQueries.add("CREATE TABLE ClassifiableTextsCharacteristics " +
        "(ClassifiableTextId INT, CharacteristicsNameId INT, CharacteristicsValueId INT, PRIMARY KEY(ClassifiableTextId, CharacteristicsNameId, CharacteristicsValueId))");
    sqlQueries.add("CREATE TABLE Vocabulary " +
        "(Id INT AUTO_INCREMENT PRIMARY KEY, Value CLOB)");

    // execute queries
    //

    try (Connection con = getConnection()) {
      Statement statement = con.createStatement();

      for (String sqlQuery : sqlQueries) {
        statement.execute(sqlQuery);
      }
    } catch (SQLException ignored) {
    }
  }
}