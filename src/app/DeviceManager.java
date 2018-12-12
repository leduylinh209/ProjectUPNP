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

public class DeviceManager {
    private Service powerService;
    private UpnpService upnpService;
    private ActionInvocation setPowerAction;
    private SubscriptionCallback deviceCallback;
    public boolean power = false;

    public DeviceManager(Service powerService, UpnpService upnpService) {
        this.powerService = powerService;
        this.upnpService = upnpService;
        this.setPowerAction = new ActionInvocation(powerService.getAction("SetTarget"));
        setRemote();
    }

    private void setRemote() {
        deviceCallback = new SubscriptionCallback(this.powerService) {
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
                Map<String, StateVariableValue> values = genaSubscription.getCurrentValues();
                StateVariableValue status = values.get("Status");
                power = (Integer.parseInt(status.toString()) == 1);
            }

            @Override
            protected void eventsMissed(GENASubscription genaSubscription, int i) {

            }
        };

        upnpService.getControlPoint().execute(deviceCallback);
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

}
