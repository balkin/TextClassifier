package com.irvil.textclassifier;

import com.irvil.textclassifier.model.ClassifiableText;
import com.irvil.textclassifier.model.VocabularyWord;
import com.irvil.textclassifier.ngram.NGramStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class VocabularyBuilder {

    private final NGramStrategy nGramStrategy;

    VocabularyBuilder(NGramStrategy nGramStrategy) {
        if (nGramStrategy == null) {
            throw new IllegalArgumentException();
        }

        this.nGramStrategy = nGramStrategy;
    }

    List<VocabularyWord> getVocabulary(List<ClassifiableText> classifiableTexts) {
        if (classifiableTexts == null ||
            classifiableTexts.size() == 0) {
            throw new IllegalArgumentException();
        }

        Map<String, Integer> uniqueValues = new HashMap<>();

        // count frequency of use each word (converted to n-gram) from all Classifiable Texts

        for (ClassifiableText classifiableText : classifiableTexts) {
            for (String word : nGramStrategy.getNGram(classifiableText.getText())) {
                if (uniqueValues.containsKey(word)) {
                    // increase counter
                    uniqueValues.put(word, uniqueValues.get(word) + 1);
                } else {
                    // add new word
                    uniqueValues.put(word, 1);
                }
            }
        }

        // convert uniqueValues to Vocabulary, excluding infrequent

        // TODO: baron - Раньше тут была проверка, что слово встречается не меньше трёх раз
        final var vocabulary = uniqueValues.keySet().stream().map(VocabularyWord::new).collect(Collectors.toList());

        log.info("Значение - кол-во повторений");
        for (Entry<String, Integer> f : uniqueValues.entrySet()) {
            log.info(f.getKey() + " - " + f.getValue());
        }

        // todo: throw exception if vocabulary is empty
        return vocabulary;
    }
}