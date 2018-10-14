package old;

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


public class ServiceManager {

    private Service power_service;
    private UpnpService upnpService;
    private ActionInvocation setPowerAction;
    private SubscriptionCallback power_binding_subscription;
    private boolean scenario_activated = false;
    public boolean power = false;

    public ServiceManager(Service power_service, UpnpService upnpService)
    {
        this.power_service = power_service;
        this.upnpService = upnpService;
        this.setPowerAction = new SetPowerAction(this.power_service);
        setup_remote();
    }

    private void setup_remote() {
        power_binding_subscription = new SubscriptionCallback(this.power_service) {
            @Override
            protected void failed(GENASubscription subscription, UpnpResponse responseStatus, Exception exception, String defaultMsg) {

            }

            @Override
            protected void established(GENASubscription subscription) {

            }

            @Override
            protected void ended(GENASubscription subscription, CancelReason reason, UpnpResponse responseStatus) {

            }

            @Override
            protected void eventReceived(GENASubscription subscription) {
                Map<String, StateVariableValue> values = subscription.getCurrentValues();
                StateVariableValue status = values.get("Status");
                boolean power_value = (Integer.parseInt(status.toString()) == 1);
                power = power_value;
            }

            @Override
            protected void eventsMissed(GENASubscription subscription, int numberOfMissedEvents) {

            }
        };
        this.upnpService.getControlPoint().execute(this.power_binding_subscription);
    }




    private void set_power(boolean value, boolean sync)
    {
        this.setPowerAction.setInput("newTargetValue", value);
        if(!sync)
        {
            ActionCallback actionCallback = new ActionCallback(this.setPowerAction) {
                @Override
                public void success(ActionInvocation invocation) {

                }

                @Override
                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {

                }
            };
            this.upnpService.getControlPoint().execute(actionCallback);
        }
        else
            new ActionCallback.Default(this.setPowerAction, upnpService.getControlPoint()).run();
    }

    public void scenario_started()
    {
        set_power(true, false);
    }
}


class SetPowerAction extends ActionInvocation {
    private UpnpService upnpService;

    public SetPowerAction(Service service)
    {
        super(service.getAction("SetTarget"));
    }
}