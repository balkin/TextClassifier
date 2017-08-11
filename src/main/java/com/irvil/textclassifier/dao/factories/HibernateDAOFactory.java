package com.irvil.textclassifier.dao.factories;

import com.irvil.textclassifier.dao.CharacteristicDAO;
import com.irvil.textclassifier.dao.ClassifiableTextDAO;
import com.irvil.textclassifier.dao.StorageCreator;
import com.irvil.textclassifier.dao.VocabularyWordDAO;
import com.irvil.textclassifier.dao.jpa.HibernateCharacteristicDAO;
import com.irvil.textclassifier.dao.jpa.HibernateClassifiableTextDAO;
import com.irvil.textclassifier.dao.jpa.HibernateDBCreator;
import com.irvil.textclassifier.dao.jpa.HibernateVocabularyWordDAO;

public class HibernateDAOFactory implements DAOFactory {
  @Override
  public ClassifiableTextDAO classifiableTextDAO() {
    return new HibernateClassifiableTextDAO();
  }

  @Override
  public CharacteristicDAO characteristicDAO() {
    return new HibernateCharacteristicDAO();
  }

  @Override
  public VocabularyWordDAO vocabularyWordDAO() {
    return new HibernateVocabularyWordDAO();
  }

  @Override
  public StorageCreator storageCreator() {
    return new HibernateDBCreator();
  }
}