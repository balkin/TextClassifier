package com.irvil.textclassifier.dao.factories;

import com.irvil.textclassifier.Config;
import com.irvil.textclassifier.dao.CharacteristicDAO;
import com.irvil.textclassifier.dao.ClassifiableTextDAO;
import com.irvil.textclassifier.dao.StorageCreator;
import com.irvil.textclassifier.dao.VocabularyWordDAO;
import com.irvil.textclassifier.dao.jdbc.connectors.JDBCConnector;
import com.irvil.textclassifier.dao.jdbc.connectors.JDBCH2Connector;
import com.irvil.textclassifier.dao.jdbc.connectors.JDBCSQLiteConnector;
import com.irvil.textclassifier.dao.jpa.EMFProvider;

import javax.persistence.EntityManagerFactory;

public interface DAOFactory {

    static DAOFactory getDaoFactory(Config config) {
        try {
            final var daoType = config.getDaoType();
            if (daoType.equals("jdbc")) {
                // create connector depends on config value
                final var dbmsType = config.getDBMSType();
                final var dbPath = config.getDbPath();
                final var dbFileName = config.getDbFileName();

                JDBCConnector jdbcConnector = null;
                if (dbmsType.equals("sqlite")) {
                    jdbcConnector = new JDBCSQLiteConnector(dbPath, dbFileName);
                }
                else if (dbmsType.equals("h2")) {
                    jdbcConnector = new JDBCH2Connector(dbPath, dbFileName);
                }
                jdbcConnector.createStorage();

                // create factory
                return new JDBCDAOFactory(jdbcConnector);
            } else if (daoType.equals("hibernate")) {
                EntityManagerFactory entityManagerFactory = EMFProvider.getInstance().getEntityManagerFactory("TextClassifier");
                return new HibernateDAOFactory(entityManagerFactory);
            }
        } catch (IllegalArgumentException e) {
            return null;
        }
        return null;
    }

    ClassifiableTextDAO classifiableTextDAO();

    CharacteristicDAO characteristicDAO();

    VocabularyWordDAO vocabularyWordDAO();

    StorageCreator storageCreator();
}