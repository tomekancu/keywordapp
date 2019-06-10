package pl.tom.keywordapp;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    public TextField linkField;

    @FXML
    public TableView tableView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Start app");
        linkField.setText("HELLO");
    }

    @FXML
    public void onCheck(){
        System.out.println("CHECK");
    }
}
