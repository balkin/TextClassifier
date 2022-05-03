package com.irvil.textclassifier;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.irvil.textclassifier.classifier.Classifier;
import com.irvil.textclassifier.dao.factories.DAOFactory;
import com.irvil.textclassifier.model.Characteristic;
import com.irvil.textclassifier.model.CharacteristicValue;
import com.irvil.textclassifier.model.ClassifiableText;
import com.irvil.textclassifier.model.VocabularyWord;
import com.irvil.textclassifier.ngram.NGramStrategy;
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

		List<Characteristic> characteristics = daoFactory.characteristicDAO().getAllCharacteristics();
		List<VocabularyWord> vocabulary = daoFactory.vocabularyWordDAO().getAll();

		for (Characteristic characteristic : characteristics) {
			String classifierPath = config.getDbPath() + "/" + characteristic.getName() + "NeuralNetworkClassifier";
			File trainedClassifier = new File(classifierPath);
			classifiers.add(new Classifier(trainedClassifier, characteristic, vocabulary, nGramStrategy));
		}
	}
	
	public static void main(String[] args) {
		try {
			TextClassifier textClassifier = new TextClassifier();
			log.info(textClassifier.classify("кредиты"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String classify(String text) throws Exception {
		ClassifiableText classifiableText = new ClassifiableText(text);

		try {
			Classifier classifier = classifiers.get(0);
			List<CharacteristicValue> classifiedValue = classifier.classify(classifiableText);
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

	private void saveNotClassifiedText(String text) throws IOException {
		BufferedWriter bw = null;
		try {
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			bw = new BufferedWriter(fw);

			bw.write("\n");
			bw.write(text);

			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bw != null) {
				bw.close();
			}
		}
	}
}
