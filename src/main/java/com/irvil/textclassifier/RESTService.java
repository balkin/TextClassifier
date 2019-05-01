package com.irvil.textclassifier;

import com.google.gson.Gson;
import com.irvil.textclassifier.classifier.Classifier;
import com.irvil.textclassifier.dao.*;
import com.irvil.textclassifier.dao.factories.DAOFactory;
import com.irvil.textclassifier.model.Characteristic;
import com.irvil.textclassifier.model.CharacteristicValue;
import com.irvil.textclassifier.model.ClassifiableText;
import com.irvil.textclassifier.model.VocabularyWord;
import com.irvil.textclassifier.ngram.NGramStrategy;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static spark.Spark.*;

public class RESTService {

    private static String TRAIN_FILE = "tt.xls";
    private static final Config config = new Config("./config/config.ini");
    private static List<Classifier> classifiers = new ArrayList<>();
    private static DAOFactory daoFactory;
    private static NGramStrategy nGramStrategy;

    public static void main(String[] args) {

        System.out.println("Init system");


        // check config file
        if (!config.isLoaded()) {
            System.out.println("Config file is not found or it is empty.");
            return;
        }

        // create DAO factory and NGramStrategy using settings from config file
        daoFactory = DAOFactory.getDaoFactory(config);
        nGramStrategy = NGramStrategy.getNGramStrategy(config.getNGramStrategy());

        if (daoFactory == null || nGramStrategy == null) {
            System.out.println("Oops, it seems there is an error in config file.");
            return;
        }


        List<Characteristic> characteristics = daoFactory.characteristicDAO().getAllCharacteristics();
        List<VocabularyWord> vocabulary = daoFactory.vocabularyWordDAO().getAll();


        boolean itFirstStart = !loadTrainedClassifiers(characteristics, vocabulary, classifiers);
        // check if it is first start
        if (itFirstStart) {
            System.out.println("WoW, firststart");

            File file = new File(TRAIN_FILE);

            if (file != null) {

                Thread t = new Thread() {
                    @Override
                    public void run() {
                        createStorage();

                        // read first sheet from a file
                        List<ClassifiableText> classifiableTexts = getClassifiableTexts(file, 1);

                        // save data to storage
                        List<VocabularyWord> vocabulary = saveVocabularyToStorage(classifiableTexts);
                        List<Characteristic> characteristics = saveCharacteristicsToStorage(classifiableTexts);
                        List<ClassifiableText> classifiableTextForTrain = saveClassifiableTextsToStorage(classifiableTexts);

                        // create and train classifiers
                        createClassifiers(characteristics, vocabulary);
                        trainAndSaveClassifiers(classifiableTextForTrain);
                        checkClassifiersAccuracy(file);

                        System.out.println("\nPlease restart the program.");
                    }
                };


                t.setUncaughtExceptionHandler((th, ex) -> System.out.println(ex.toString()));
                t.start();
            }


        }else{

        }

        System.out.println("Starting REST server");
        port(8081);

        get("/state", (req,res) -> {
           return (itFirstStart)?"restart":"ok";
        });


        post("/classify",(req,res) ->{
            if(itFirstStart){
                return "restart";
            }else{


                String classifyText = req.queryMap().get("str").value();

                System.out.println("POST:/classify, str = " + classifyText);

                String str = "";
                ClassifiableText classifiableText = new ClassifiableText(classifyText);
                StringBuilder classifiedCharacteristics = new StringBuilder();

                // start Classifier for each Characteristic from DB
                //

                Gson gson = new Gson();
                try {
                    for (Classifier classifier : classifiers) {
                        List<CharacteristicValue> characteristics1 = classifier.classify(classifiableText);
                        return gson.toJson(characteristics1);

                    }
                } catch (Exception e) {
                    // it is possible if DB was edited manually
                    /*return("It seems that trained classifier does not match Characteristics and Vocabulary. " +
                            "You need to retrain classifier.");*/
                    return("{error:\"not_classified\"");
                }


                return  str;



            }
        });







    }


    private static void checkClassifiersAccuracy(File file) {
        System.out.println("\n");

        // read second sheet from a file
        List<ClassifiableText> classifiableTexts = getClassifiableTexts(file, 2);

        for (Classifier classifier : classifiers) {
            Characteristic characteristic = classifier.getCharacteristic();
            int correctlyClassified = 0;

            for (ClassifiableText classifiableText : classifiableTexts) {
                CharacteristicValue idealValue = classifiableText.getCharacteristicValue(characteristic.getName());
                CharacteristicValue classifiedValue = classifier.classify(classifiableText).get(0);

                if (classifiedValue.getValue().equals(idealValue.getValue())) {
                    correctlyClassified++;
                }
            }

            double accuracy = ((double) correctlyClassified / classifiableTexts.size()) * 100;
            System.out.println(String.format("Accuracy of Classifier for '" + characteristic.getName() + "' characteristic: %.2f%%", accuracy));
        }
    }


    private static boolean loadTrainedClassifiers(List<Characteristic> characteristics, List<VocabularyWord> vocabulary, List<Classifier> classifiers) {
        if (characteristics.size() == 0 || vocabulary.size() == 0) {
            return false;
        }

        // load trained classifiers for each Characteristics
        try {
            for (Characteristic characteristic : characteristics) {
                File trainedClassifier = new File(config.getDbPath() + "/" + characteristic.getName() + "NeuralNetworkClassifier");
                classifiers.add(new Classifier(trainedClassifier, characteristic, vocabulary, nGramStrategy));
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }


    private static void createClassifiers(List<Characteristic> characteristics, List<VocabularyWord> vocabulary) {
        for (Characteristic characteristic : characteristics) {
            Classifier classifier = new Classifier(characteristic, vocabulary, nGramStrategy);
//            classifier.addObserver(logWindow);
            classifiers.add(classifier);
        }
    }

    private static void createStorage() {
        StorageCreator storageCreator = daoFactory.storageCreator();
        storageCreator.createStorageFolder(config.getDbPath());
        storageCreator.createStorage();
        System.out.println("Storage created. Wait...");
    }

    private boolean loadTrainedClassifiers(List<Characteristic> characteristics, List<VocabularyWord> vocabulary) {
        if (characteristics.size() == 0 || vocabulary.size() == 0) {
            return false;
        }

        // load trained classifiers for each Characteristics
        //

        try {
            for (Characteristic characteristic : characteristics) {
                File trainedClassifier = new File(config.getDbPath() + "/" + characteristic.getName() + "NeuralNetworkClassifier");
                classifiers.add(new Classifier(trainedClassifier, characteristic, vocabulary, nGramStrategy));
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private static void trainAndSaveClassifiers(List<ClassifiableText> classifiableTextForTrain) {
        for (Classifier classifier : classifiers) {
            classifier.train(classifiableTextForTrain);
            classifier.saveTrainedClassifier(new File(config.getDbPath() + "/" + classifier.toString()));
        }

        Classifier.shutdown();
    }

    private static List<ClassifiableText> saveClassifiableTextsToStorage(List<ClassifiableText> classifiableTexts) {
        ClassifiableTextDAO classifiableTextDAO = daoFactory.classifiableTextDAO();

        try {
            classifiableTextDAO.addAll(classifiableTexts);
            System.out.println("Classifiable texts saved. Wait...");
        } catch (NotExistsException e) {
            System.out.println(e.getMessage());
        }

        // return classifiable texts from DB
        return classifiableTextDAO.getAll();
    }

    private static List<Characteristic> saveCharacteristicsToStorage(List<ClassifiableText> classifiableTexts) {
        Set<Characteristic> characteristics = getCharacteristicsCatalog(classifiableTexts);

        CharacteristicDAO characteristicDAO = daoFactory.characteristicDAO();

        for (Characteristic characteristic : characteristics) {
            try {
                characteristicDAO.addCharacteristic(characteristic);
                System.out.println("'" + characteristic.getName() + "' characteristic saved. Wait...");
            } catch (AlreadyExistsException e) {
                System.out.println(e.getMessage());
            }
        }

        // return Characteristics with IDs
        return characteristicDAO.getAllCharacteristics();
    }

    private static Set<Characteristic> getCharacteristicsCatalog(List<ClassifiableText> classifiableTexts) {
        Map<Characteristic, Characteristic> characteristics = new HashMap<>();

        for (ClassifiableText classifiableText : classifiableTexts) {
            // for all classifiable texts characteristic values
            //

            for (Map.Entry<Characteristic, CharacteristicValue> entry : classifiableText.getCharacteristics().entrySet()) {
                // add characteristic to catalog
                characteristics.put(entry.getKey(), entry.getKey());

                // add characteristic value to possible values
                characteristics.get(entry.getKey()).addPossibleValue(entry.getValue());
            }
        }

        return characteristics.keySet();
    }

    private static List<VocabularyWord> saveVocabularyToStorage(List<ClassifiableText> classifiableTexts) {
        VocabularyWordDAO vocabularyWordDAO = daoFactory.vocabularyWordDAO();

        try {
            vocabularyWordDAO.addAll(new VocabularyBuilder(nGramStrategy).getVocabulary(classifiableTexts));
            System.out.println("Vocabulary saved. Wait...");
        } catch (AlreadyExistsException e) {
            System.out.println(e.getMessage());
        }

        // return vocabulary with IDs
        return vocabularyWordDAO.getAll();
    }

    private static List<ClassifiableText> getClassifiableTexts(File file, int sheetNumber) {
        List<ClassifiableText> classifiableTexts = new ArrayList<>();

        try {
            classifiableTexts = new ExcelFileReader().xlsxToClassifiableTexts(file, sheetNumber);
        } catch (IOException | EmptySheetException e) {
            System.out.println(e.getMessage());
        }

        return classifiableTexts;
    }


}
