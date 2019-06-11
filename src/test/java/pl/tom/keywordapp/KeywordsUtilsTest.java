package pl.tom.keywordapp;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class KeywordsUtilsTest {

    // test links
    // https://www.metatags.org/meta_name_keywords
    // https://www.w3schools.com/tags/tag_meta.asp

    @Test
    public void getKeywordsFromDocument() {
        String html = "<html>" +
                "<head>" +
                "<meta name=\"keywords\" content=\"meta, meta tags, keyword\">" +
                "</head>" +
                "</html>";
        Document doc = Jsoup.parse(html);
        Set<String> set = KeywordsUtils.getKeywordsFromDocument(doc);
        Set<String> expected = Set.of("meta", "meta tags", "keyword");
        assertEquals(expected, set);
    }

    @Test
    public void getKeywordsFromDocumentEmpty() {
        String html = "<html>" +
                "<head>" +
                "</head>" +
                "</html>";
        Document doc = Jsoup.parse(html);
        Set<String> set = KeywordsUtils.getKeywordsFromDocument(doc);
        assertTrue(set.isEmpty());
    }

    @Test
    public void getKeywordsFromDocumentNoParsableHtml() {
        String html = "ml>";
        Document doc = Jsoup.parse(html);
        Set<String> set = KeywordsUtils.getKeywordsFromDocument(doc);
        assertTrue(set.isEmpty());
    }

    @Test(expected = KeywordsUtils.NoKeywordsException.class)
    public void countEmpty() throws KeywordsUtils.NoKeywordsException {
        String html = "<html>" +
                "<head>" +
                "</head>" +
                "</html>";
        Document doc = Jsoup.parse(html);
        KeywordsUtils.count(doc);
    }

    public <T> void assertList(List<T> expected, List<T> list) {
        assertEquals(expected.size(), list.size());
        for (int i = 0; i < expected.size(); i++)
            assertEquals(expected.get(i), list.get(i));
    }

    @Test
    public void count() throws KeywordsUtils.NoKeywordsException {
        String html = "<html>" +
                "<head>" +
                "<meta name=\"keywords\" content=\"meta, meta tags, keyword\">" +
                "</head>" +
                "</html>";
        Document doc = Jsoup.parse(html);
        List<Keyword> list = KeywordsUtils.count(doc);
        List<Keyword> expected = List.of(new Keyword("meta", 0),
                new Keyword("meta tags", 0),
                new Keyword("keyword", 0));
        assertList(expected, list);
    }

    @Test
    public void count2() throws KeywordsUtils.NoKeywordsException {
        String html = "<html>" +
                "<head>" +
                "<meta name=\"keywords\" content=\"meta, meta tags, keyword\">" +
                "</head>" +
                "meta tags, keyword" +
                "<p>ha ha keywords</p>" +
                "</html>";
        Document doc = Jsoup.parse(html);
        List<Keyword> list = KeywordsUtils.count(doc);
        List<Keyword> expected = List.of(new Keyword("meta", 1),
                new Keyword("meta tags", 1),
                new Keyword("keyword", 2));
        assertList(expected, list);
    }

    @Test
    public void findWords() {
        List<Integer> result = KeywordsUtils.findWordsCaseSensitive("or and or", "or");

        List<Integer> expected = List.of(0, 7);
        assertList(expected, result);
    }

    @Test
    public void findWords2() {
        List<Integer> result = KeywordsUtils.findWordsCaseSensitive("or and or\n" +
                "heLp", "heLp");
        Assert.assertEquals(1, result.size());


        result = KeywordsUtils.findWordsCaseSensitive("or and or\n" +
                "heLp", "help");
        Assert.assertEquals(0, result.size());
    }
}
