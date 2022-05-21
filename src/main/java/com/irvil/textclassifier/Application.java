package com.irvil.textclassifier;

import com.irvil.textclassifier.dao.AlreadyExistsException;
import com.irvil.textclassifier.dao.CharacteristicDAO;
import com.irvil.textclassifier.dao.ClassifiableTextDAO;
import com.irvil.textclassifier.dao.factories.DAOFactory;
import com.irvil.textclassifier.model.Characteristic;
import com.irvil.textclassifier.ngram.NGramStrategy;
import java.io.File;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

import static com.irvil.textclassifier.RESTService.getCharacteristicsCatalog;
import static com.irvil.textclassifier.RESTService.getClassifiableTexts;

@Slf4j
public class Application {

    public static void main(String[] args) throws Exception {
        new Application().run(args);
    }

    private Config config = new Config(System.getProperty("user.dir") + "/../TextClassifier/config/config.ini");


    private void run(String[] args) throws Exception {
        if (!config.isLoaded()) {
            throw new Exception("Config is not load!");
        }

        final DAOFactory daoFactory = DAOFactory.getDaoFactory(config);
        final NGramStrategy nGramStrategy = NGramStrategy.getNGramStrategy(config.getNGramStrategy());

        if (daoFactory == null || nGramStrategy == null) {
            throw new Exception("Oops, it seems there is an error in config file.");
        }

        for (String arg : args) {
            final var file = new File(arg);
            final var classifiableTexts = getClassifiableTexts(file, 1);
            daoFactory.vocabularyWordDAO().addAll(new VocabularyBuilder(nGramStrategy).getVocabulary(classifiableTexts));
            Set<Characteristic> characteristics = getCharacteristicsCatalog(classifiableTexts);
            CharacteristicDAO characteristicDAO = daoFactory.characteristicDAO();
            for (Characteristic characteristic : characteristics) {
                try {
                    characteristicDAO.addCharacteristic(characteristic);
                }
                catch (AlreadyExistsException ignored) {
                    log.debug("Duplicate characteristic: {}", characteristic);
                }
            }
            ClassifiableTextDAO classifiableTextDAO = daoFactory.classifiableTextDAO();
            classifiableTextDAO.addAll(classifiableTexts);
            log.info("Vocabulary: {}", characteristics);
        }
    }
}
