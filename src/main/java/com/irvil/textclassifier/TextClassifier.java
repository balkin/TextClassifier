package com.irvil.textclassifier;

import com.irvil.textclassifier.classifier.Classifier;
import com.irvil.textclassifier.dao.factories.DAOFactory;
import com.irvil.textclassifier.model.Characteristic;
import com.irvil.textclassifier.model.CharacteristicValue;
import com.irvil.textclassifier.model.ClassifiableText;
import com.irvil.textclassifier.model.VocabularyWord;
import com.irvil.textclassifier.ngram.NGramStrategy;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TextClassifier {

    private static final String NOT_CLASSIFIED_TEXT_FILE = "/test_db/NotClassifiedText.txt";

    private Config config = new Config(System.getProperty("user.dir") + "/../TextClassifier/config/config.ini");
    private final List<Classifier> classifiers = new ArrayList<>();
    private DAOFactory daoFactory;
    private NGramStrategy nGramStrategy;
    private File file = new File(System.getProperty("user.dir") + "/../TextClassifier" + NOT_CLASSIFIED_TEXT_FILE);

    public TextClassifier() throws Exception {
        if (!config.isLoaded()) {
            throw new Exception("Config is not load!");
        }

        daoFactory = DAOFactory.getDaoFactory(config);
        nGramStrategy = NGramStrategy.getNGramStrategy(config.getNGramStrategy());

        if (daoFactory == null || nGramStrategy == null) {
            throw new Exception("Oops, it seems there is an error in config file.");
        }

        final List<Characteristic> characteristics = daoFactory.characteristicDAO().getAllCharacteristics();
        final List<VocabularyWord> vocabulary = daoFactory.vocabularyWordDAO().getAll();

        for (Characteristic characteristic : characteristics) {
            File tf = Path.of(config.getDbPath(), characteristic.getName() + "NeuralNetworkClassifier").toFile();
            classifiers.add(new Classifier(tf, characteristic, vocabulary, nGramStrategy));
        }
    }

    public static void main(String[] args) {
        try {
            final TextClassifier textClassifier = new TextClassifier();
            log.info(textClassifier.classify("кредиты"));
        } catch (Exception e) {
            log.error("Exception happened", e);
        }
    }

    public String classify(String text) throws Exception {
        final ClassifiableText classifiableText = new ClassifiableText(text);

        try {
            final Classifier classifier = classifiers.get(0);
            final List<CharacteristicValue> classifiedValue = classifier.classify(classifiableText);
            if (classifiedValue == null) {
                saveNotClassifiedText(text);
                return null;
            }

            return classifiedValue.get(0).getValue();
        } catch (Exception e) {
            throw new Exception("It seems that trained classifier does not match Characteristics and Vocabulary. "
                + "You need to retrain classifier.");
        }
    }

    private void saveNotClassifiedText(String text) {
        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            final FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
            try (BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write("\n");
                bw.write(text);
            }
        } catch (Exception e) {
            log.error("Failed to save non-classified text", e);
        }
    }
}
