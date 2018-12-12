package app;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.controlpoint.SubscriptionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.state.StateVariableValue;

import java.util.Map;

public abstract class AlarmManager {
    private Service powerService;
    private UpnpService upnpService;
    private ActionInvocation setPowerAction;
    private SubscriptionCallback alarmCallback;

    public AlarmManager(Service powerService, UpnpService upnpService) {
        this.powerService = powerService;
        this.upnpService = upnpService;
        this.setPowerAction = new ActionInvocation(powerService.getAction("SetTarget"));
        setRemote();
    }

    private void setRemote() {
        alarmCallback = new SubscriptionCallback(powerService) {
            @Override
            protected void failed(GENASubscription genaSubscription, UpnpResponse upnpResponse, Exception e, String s) {

            }

            @Override
            protected void established(GENASubscription genaSubscription) {

            }

            @Override
            protected void ended(GENASubscription genaSubscription, CancelReason cancelReason, UpnpResponse upnpResponse) {

            }

            @Override
            protected void eventReceived(GENASubscription genaSubscription) {
                System.out.println("Event: " + genaSubscription.getCurrentSequence().getValue());
                Map<String, StateVariableValue> values = genaSubscription.getCurrentValues();
                StateVariableValue status = values.get("Status");
                System.out.println("Statuuccessfully called actions is: " + status.toString());
                if (Integer.parseInt(status.toString()) == 1) {
                    onAlarmOn();
                } else {
                    onAlarmOff();
                }
            }

            @Override
            protected void eventsMissed(GENASubscription genaSubscription, int i) {

            }
        };

        upnpService.getControlPoint().execute(alarmCallback);
    }

    public void setPower(boolean value) {
        setPowerAction.setInput("newTargetValue", value);
        ActionCallback actionCallback = new ActionCallback(setPowerAction) {
            @Override
            public void success(ActionInvocation actionInvocation) {
                System.out.println("Successfully called action ...");
            }

            @Override
            public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String s) {

            }
        };
        upnpService.getControlPoint().execute(actionCallback);
    }

    public abstract void onAlarmOff();

    public abstract void onAlarmOn();
}
