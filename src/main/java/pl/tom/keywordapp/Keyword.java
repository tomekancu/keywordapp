package pl.tom.keywordapp;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Keyword {

    private StringProperty name;
    private IntegerProperty count;

    public Keyword(String name, int count) {
        setName(name);
        setCount(count);
    }

    public void setName(String value) {
        nameProperty().set(value);
    }

    public String getName() {
        return nameProperty().get();
    }

    public StringProperty nameProperty() {
        if (name == null) name = new SimpleStringProperty(this, "name");
        return name;
    }

    public void setCount(int value) {
        countProperty().set(value);
    }

    public int getCount() {
        return countProperty().get();
    }

    public IntegerProperty countProperty() {
        if (count == null) count = new SimpleIntegerProperty(this, "count");
        return count;
    }
}
