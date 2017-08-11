package com.irvil.textclassifier.dao.jpa;

import com.irvil.textclassifier.Config;
import com.irvil.textclassifier.dao.VocabularyWordDAOTest;
import com.irvil.textclassifier.dao.factories.DAOFactory;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HibernateVocabularyWordDAOTest extends VocabularyWordDAOTest {
  @Override
  public DAOFactory createDAOFactory() {
    Config cfg = mock(Config.class);
    when(cfg.getDaoType()).thenReturn("hibernate");

    return DAOFactory.getDaoFactory(cfg);
  }
}