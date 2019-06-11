package pl.tom.keywordapp;

import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class KeywordUtils {
    public static Set<String> getKeywordsFromDocument(Document document) {
        return document.head().select("meta").stream()
                .filter(e -> "keywords".equals(e.attr("name").toLowerCase().trim()))
                .map(e -> e.attr("content"))
                .flatMap(e -> Arrays.stream(e.split(",")))
                .map(String::trim)
                .filter(key -> key.length() > 0)
                .collect(Collectors.toSet());
    }

    public static List<Keyword> countKeywords(Document document) throws NoKeywordsException {
        Set<String> keywords = KeywordUtils.getKeywordsFromDocument(document);
        if (keywords.size() == 0) {
            throw new NoKeywordsException();
        }
        return KeywordUtils.countKeywords(document, keywords);
    }

    public static List<Keyword> countKeywords(Document document, Set<String> keywords) throws NoKeywordsException  {
        if (keywords.size() == 0) {
            throw new NoKeywordsException();
        }

        String body = document.body().text();
        return keywords.stream()
                .map(key -> new Keyword(key, findWordsCaseSensitive(body, key).size()))
                .collect(Collectors.toList());
    }

    public static List<Integer> findWordsCaseSensitive(String textString, String word) {
        List<Integer> indexes = new ArrayList<>();
        int wordLength = 0;

        int index = 0;
        while (index != -1) {
            index = textString.indexOf(word, index + wordLength);
            if (index != -1) {
                indexes.add(index);
            }
            wordLength = word.length();
        }
        return indexes;
    }

    public static class NoKeywordsException extends Exception {

    }
}
