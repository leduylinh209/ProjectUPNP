package app;

import javafx.application.Platform;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.controlpoint.SubscriptionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.state.StateVariableValue;
import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.model.types.ServiceId;
import org.fourthline.cling.model.types.UDAServiceId;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;

import java.util.Map;

public class test implements Runnable {

    private Controller controller;

    public test(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        try {

            UpnpService upnpService = new UpnpServiceImpl();

            // Add a listener for device registration events
            upnpService.getRegistry().addListener(
                    createRegistryListener(upnpService)
            );
            // Broadcast a search message for all devices
            upnpService.getControlPoint().search(
                    new STAllHeader()
            );
        } catch (Exception ex) {
            System.err.println("Exception occured: " + ex);
            System.exit(1);
        }
    }

    // DOC: REGISTRYLISTENER
    RegistryListener createRegistryListener(final UpnpService upnpService) {
        return new DefaultRegistryListener() {

            ServiceId serviceId = new UDAServiceId("SwitchStatus");


            // Chạy đầu tiên
            // Phát hiện ra con remote chỵ service gì
            @Override
            public void remoteDeviceAdded(Registry registry, RemoteDevice device) {

                // Change Status of alarm in control panel,
                // Không hiểu lắm về hàm runLater của javafx, hàm này làm alarm nhảy số @@
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        controller.changeText();
                    }
                });

                Service switchStatus;
                if ((switchStatus = device.findService(serviceId)) != null) {

                    System.out.println("Service discovered: " + switchStatus);
                    // Khi phát hiện ra, gọi đến một hành động
                    executeAction(upnpService, switchStatus);

                    // Đăng ký lắng nghe service mới
                    SubscriptionCallback callback = new SubscriptionCallback(switchStatus, 600) { // Timeout in seconds

                        // Hành động khi service có sự thay đổi
                        public void eventReceived(GENASubscription sub) {
                            System.out.println("Event: " + sub.getCurrentSequence().getValue());
                            Map<String, StateVariableValue> values = sub.getCurrentValues();
                            StateVariableValue status = values.get("Status");
                            System.out.println("Statuuccessfully called actions is: " + status.toString());
                        }

                        public void established(GENASubscription sub) {
                            System.out.println("Established: " + sub.getSubscriptionId());
                        }

                        public void ended(GENASubscription sub, CancelReason reason, UpnpResponse response) {
                            // Reason should be null, or it didn't end regularly
                        }

                        public void eventsMissed(GENASubscription sub, int numberOfMissedEvents) {
                            System.out.println("Missed events: " + numberOfMissedEvents);
                        }

                        @Override
                        protected void failed(GENASubscription genas, UpnpResponse ur, Exception excptn, String string) {
                            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                        }
                    };

                    upnpService.getControlPoint().execute(callback);
                }

            }

            @Override
            public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
                Service switchPower;
                // Change Status of alarm in control panel,
                // Không hiểu lắm về hàm runLater của javafx, hàm này làm alarm nhảy số @@
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        controller.changeTextOffline();
                    }
                });
                if ((switchPower = device.findService(serviceId)) != null) {
                    System.out.println("Service disappeared: " + switchPower);
                }
            }

        };
    }
    // DOC: REGISTRYLISTENER
    // DOC: EXECUTEACTION
    void executeAction(UpnpService upnpService, Service switchPowerService) {

        ActionInvocation setTargetInvocation =
                new SetTargetActionInvocation(switchPowerService);

        // Executes asynchronous in the background
        upnpService.getControlPoint().execute(
                new ActionCallback(setTargetInvocation) {

                    @Override
                    public void success(ActionInvocation invocation) {
                        assert invocation.getOutput().length == 0;
                        System.out.println("Successfully called action!");
                    }

                    @Override
                    public void failure(ActionInvocation invocation,
                                        UpnpResponse operation,
                                        String defaultMsg) {
                        System.err.println(defaultMsg);
                    }
                }
        );

    }

    class SetTargetActionInvocation extends ActionInvocation {

        SetTargetActionInvocation(Service service) {
            super(service.getAction("SetTarget"));
            try {

                // Throws InvalidValueException if the value is of wrong type
                setInput("NewTargetValue", true);

            } catch (InvalidValueException ex) {
                System.err.println(ex.getMessage());
                System.exit(1);
            }
        }
    }
}
