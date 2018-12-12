package app;

import device.Alarm;
import javafx.fxml.FXML;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;

public class Controller {
    @FXML
    private Label alarmStatusLb;
    @FXML
    private ToggleButton alarmToggleButton;

    public void changeAlarmTextOn() {
        alarmStatusLb.setText("Online");
    }

    public void alarmButtonClicked() {
        if (alarmToggleButton.isSelected()) {
            alarmToggleButton.setText("OFF");
            ControlPoint.alarm.setPower(true);
        } else {
            alarmToggleButton.setText("ON");
            ControlPoint.alarm.setPower(false);
        }
    }

    public void changeAlarmTextOff() {
        alarmStatusLb.setText("Offline");
    }

    public void onAlarmOn() {
        alarmToggleButton.setSelected(true);
        alarmToggleButton.setText("OFF");
    }

    public void onAlarmOff() {
        alarmToggleButton.setSelected(false);
        alarmToggleButton.setText("ON");
    }

}


