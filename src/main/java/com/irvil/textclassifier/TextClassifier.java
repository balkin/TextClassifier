package com.irvil.textclassifier;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.irvil.textclassifier.classifier.Classifier;
import com.irvil.textclassifier.dao.factories.DAOFactory;
import com.irvil.textclassifier.model.Characteristic;
import com.irvil.textclassifier.model.CharacteristicValue;
import com.irvil.textclassifier.model.ClassifiableText;
import com.irvil.textclassifier.model.VocabularyWord;
import com.irvil.textclassifier.ngram.NGramStrategy;

public class TextClassifier {

	private final Config config = new Config("./config/config.ini");
	private final List<Classifier> classifiers = new ArrayList<>();
	private DAOFactory daoFactory;
	private NGramStrategy nGramStrategy;

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
			File trainedClassifier = new File(
					config.getDbPath() + "/" + characteristic.getName() + "NeuralNetworkClassifier");
			classifiers.add(new Classifier(trainedClassifier, characteristic, vocabulary, nGramStrategy));
		}
	}
	
	public static void main(String[] args) {
		try {
			TextClassifier textClassifier = new TextClassifier();
			System.out.println(textClassifier.classify("кредиты"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String classify(String text) throws Exception {
		ClassifiableText classifiableText = new ClassifiableText(text);

		try {
			Classifier classifier = classifiers.get(0);
			CharacteristicValue classifiedValue = classifier.classify(classifiableText);
			if (classifiedValue == null) {
				return null;
			}
			
			return classifiedValue.getValue();
		} catch (Exception e) {
			throw new Exception("It seems that trained classifier does not match Characteristics and Vocabulary. "
					+ "You need to retrain classifier.");
		}
	}
}
