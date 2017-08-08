package com.irvil.textclassifier.dao.jdbc;

import com.irvil.textclassifier.Config;
import com.irvil.textclassifier.dao.CharacteristicDAOTest;
import com.irvil.textclassifier.dao.factories.DAOFactory;

public class JDBCH2CharacteristicDAOTest extends CharacteristicDAOTest {
  @Override
  public DAOFactory createDAOFactory() {
    return DAOFactory.getDaoFactory(new Config("./test_db/test_config_h2.ini"));
  }
}