package pl.tom.keywordapp;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    public TextField urlField;
    @FXML
    public TableView<Keyword> tableView;
    @FXML
    public TableColumn<Keyword, String> nameColumn;
    @FXML
    public TableColumn<Keyword, Number> countColumn;
    @FXML
    public Button checkButton;

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
        String urlString = urlField.getText().trim();
        urlField.setText(urlString);

        DownloadPageTask task = new DownloadPageTask(urlString, keywordsData, checkButton);
        new Thread(task).start();
    }

    public static class DownloadPageTask extends Task<List<Keyword>> {

        private final String urlString;
        private final ObservableList<Keyword> updateList;
        private final Button checkButton;

        public DownloadPageTask(String urlString, ObservableList<Keyword> updateList, Button checkButton) {
            this.urlString = urlString;
            this.updateList = updateList;
            this.checkButton = checkButton;
        }

        @Override
        protected List<Keyword> call() throws URISyntaxException, IllegalArgumentException, IOException, InterruptedException, KeywordsUtils.NoKeywordsException {
            checkButton.disableProperty().setValue(true);
            URI uri = new URI(urlString);

            HttpClient httpClient = HttpClient.newBuilder().build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            Document document = Jsoup.parse(response.body(), urlString);

            return KeywordsUtils.count(document);
        }

        @Override
        protected void succeeded() {
            super.succeeded();
            checkButton.disableProperty().setValue(false);
            updateList.setAll(getValue());
        }

        @Override
        protected void cancelled() {
            super.cancelled();
            checkButton.disableProperty().setValue(false);
        }

        @Override
        protected void failed() {
            super.failed();
            checkButton.disableProperty().setValue(false);
            if (getException() instanceof URISyntaxException) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("URL parse problem");
                alert.setContentText(getException().toString());
                alert.show();
            } else if (getException() instanceof IllegalArgumentException) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("bad URL scheme");
                alert.setContentText(getException().toString());
                alert.show();
            } else if (getException() instanceof IOException || getException() instanceof InterruptedException) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("downloading page error");
                alert.setContentText(getException().toString());
                alert.show();
            } else if (getException() instanceof KeywordsUtils.NoKeywordsException){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Info");
                alert.setHeaderText("no keywords on the website");
                alert.show();
            } else{
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("undefined error");
                alert.setContentText(getException().toString());
                alert.show();
            }
        }

    }
}
