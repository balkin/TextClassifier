package com.irvil.textclassifier.dao.jdbc;

import com.irvil.textclassifier.Config;
import com.irvil.textclassifier.dao.VocabularyWordDAOTest;
import com.irvil.textclassifier.dao.factories.DAOFactory;

public class JDBCH2VocabularyWordDAOTest extends VocabularyWordDAOTest {
  @Override
  public DAOFactory createDAOFactory() {
    return DAOFactory.getDaoFactory(new Config("./test_db/test_config_h2.ini"));
  }
}