package pl.tom.keywordapp;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;

public class Controller implements Initializable {

    @FXML
    public TextField urlField;
    @FXML
    public TableView<Keyword> tableView;
    @FXML
    public TableColumn<Keyword, String> nameColumn;
    @FXML
    public TableColumn<Keyword, Number> countColumn;

    private ObservableList<Keyword> keywordsData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        SortedList<Keyword> sortedData = new SortedList<>(keywordsData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);
        Platform.runLater(() -> tableView.requestFocus()); // default focus set on textfield, i don't need that

        nameColumn.setCellValueFactory(p -> p.getValue().nameProperty());
        countColumn.setCellValueFactory(p -> p.getValue().countProperty());

        urlField.setText("https://www.metatags.org/meta_name_keywords");
    }

    @FXML
    public void onCheck() {
        // test links
        // https://www.metatags.org/meta_name_keywords
        // https://adsecur.com/seo/kompendium/slowa-kluczowe
        // https://www.w3schools.com/tags/tag_meta.asp

        String urlString = urlField.getText().trim();
        urlField.setText(urlString);
        System.out.println(urlString);

        URI uri;
        try {
            uri = new URI(urlString);
        } catch (URISyntaxException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("URL parse problem");
            alert.setContentText(e.toString());
            alert.show();
            return;
        }

        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                    .uri(uri)
                    .build();
        } catch (IllegalArgumentException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("bad URL scheme");
            alert.setContentText(e.toString());
            alert.show();
            return;
        }

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("downloading page error");
            alert.setContentText(e.toString());
            alert.show();
            return;
        } catch (InterruptedException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("downloading page error");
            alert.setContentText(e.toString());
            alert.show();
            return;
        }

        Document document = Jsoup.parse(response.body(), urlString);

        Elements meta = document.head().select("meta");

        Set<String> keywords = meta.stream()
                .filter(e -> "keywords".equals(e.attr("name").toLowerCase().trim()))
                .map(e -> e.attr("content"))
                .flatMap(e -> Arrays.stream(e.split(",")))
                .map(s -> s.trim())
                .collect(Collectors.toSet());

        String body = document.body().toString();

        List<Keyword> countedKeywords = keywords.stream()
                .map(key -> new Keyword(key, findWords(body, key).size()))
                .collect(Collectors.toList());

        keywordsData.setAll(countedKeywords);
    }

    public List<Integer> findWords(String textString, String word) {
        List<Integer> indexes = new ArrayList<>();
        String lowerCaseTextString = textString.toLowerCase();
        String lowerCaseWord = word.toLowerCase();
        int wordLength = 0;

        int index = 0;
        while (index != -1) {
            index = lowerCaseTextString.indexOf(lowerCaseWord, index + wordLength);
            if (index != -1) {
                indexes.add(index);
            }
            wordLength = word.length();
        }
        return indexes;
    }
}
