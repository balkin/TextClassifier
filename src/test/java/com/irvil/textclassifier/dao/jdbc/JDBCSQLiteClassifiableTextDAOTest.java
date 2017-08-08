package com.irvil.textclassifier.dao.jdbc;

import com.irvil.textclassifier.Config;
import com.irvil.textclassifier.dao.ClassifiableTextDAOTest;
import com.irvil.textclassifier.dao.factories.DAOFactory;

public class JDBCSQLiteClassifiableTextDAOTest extends ClassifiableTextDAOTest {
  @Override
  public DAOFactory createDAOFactory() {
    return DAOFactory.getDaoFactory(new Config("./test_db/test_config_sqlite.ini"));
  }
}