package app;

import javafx.fxml.FXML;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;

public class Controller {

    @FXML
    private Label alarmStatusLb;

    public Controller() {
        System.out.println("abc");
    }

    public void  changeText() {
        alarmStatusLb.setText("abc");
    }

    public void changeStatus(ActionEvent actionEvent) {
        String name = "Online";
        if(alarmStatusLb != null) {
            alarmStatusLb.setText(name);
        } else {
            System.out.println("abc");
        }
    }
}


