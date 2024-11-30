package com.adsolutions.englishapp;

import com.adsolutions.englishapp.enums.Language;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class AnswerToQuestionTransformer {

    public static void buildDictionary(Language fromLang, Language toLang) {
        if (fromLang.equals(toLang)) {
            return;
        }

        Map<String, List<String>> dictionaryBase = getDirectoryMap(fromLang);
        Map<String, List<String>> dictionaryFinal = getDirectoryMap(toLang);

        dictionaryBase.forEach((baseQuestion, baseAnswerList) -> {
            baseAnswerList.forEach(baseAnswer -> {
                Optional<Map.Entry<String, List<String>>> foundQuestionEntryFinal =
                        dictionaryFinal.entrySet().stream().filter(
                                entryFinal -> entryFinal.getKey().equals(baseAnswer)).findFirst();
                if (foundQuestionEntryFinal.isPresent()) {
                    if (foundQuestionEntryFinal.get().getValue().stream()
                            .noneMatch(baseQuestion::equals)) {
                        Objects.requireNonNull(dictionaryFinal.get(baseAnswer)).add(baseQuestion);
                    }
                } else {
                    dictionaryFinal.put(baseAnswer, new ArrayList<>(Collections.singletonList(baseQuestion)));
                }
            });
        });
    }

    private static Map<String, List<String>> getDirectoryMap(Language lang) {
        return switch (lang) {
            case EN -> QuestionManager.getDictionaryMapEn();
            case PL -> QuestionManager.getDictionaryMapPl();
        };
    }
}