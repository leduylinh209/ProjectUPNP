package app;

import javafx.fxml.FXML;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;

public class Controller {

    @FXML
    private Label alarmStatusLb;

    public void  changeText() {
        alarmStatusLb.setText("Online");
    }

    public void changeStatus(ActionEvent actionEvent) {
        String name = "Online";
        if(alarmStatusLb != null) {
            alarmStatusLb.setText(name);
        } else {
            System.out.println("abc");
        }
    }

    public void changeTextOffline() {
        alarmStatusLb.setText("Offline");
    }
}


