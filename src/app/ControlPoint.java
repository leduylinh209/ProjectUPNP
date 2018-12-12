package app;

import com.sun.xml.internal.bind.v2.model.annotation.RuntimeAnnotationReader;
import device.Alarm;
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
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDAServiceId;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;

import java.util.ArrayList;
import java.util.Map;

public class ControlPoint implements Runnable {

    private Controller controller;
    static UpnpService upnpService = new UpnpServiceImpl();
    public static ArrayList<DeviceManager> devices = new ArrayList<>();
    public static AlarmManager alarm;

    public ControlPoint(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        try {

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

                Service switchStatus;
                if ((switchStatus = device.findService(serviceId)) != null) {

                    if (device.getType().equals(new UDADeviceType("Alarm"))) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                controller.changeAlarmTextOn();
                            }
                        });
                    }
                    System.out.println("Service discovered: " + switchStatus);
                    // Khi phát hiện ra, gọi đến một hành động
//                    executeAction(upnpService, switchStatus);
                    if (device.getType().equals(new UDADeviceType("Alarm"))) {
                        // Đăng ký lắng nghe service mới
                        alarm = new AlarmManager(switchStatus, upnpService) {
                            @Override
                            public void onAlarmOff() {
                                alarmNotify();
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        controller.onAlarmOff();
                                    }
                                });
                            }

                            @Override
                            public void onAlarmOn() {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        controller.onAlarmOn();
                                    }
                                });
                            }
                        };
                    } else {
                        devices.add(new DeviceManager(switchStatus, upnpService));
                    }
                }

            }

            @Override
            public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
                Service switchPower;
                // Change Status of alarm in control panel,
                // Không hiểu lắm về hàm runLater của javafx, hàm này làm alarm nhảy số @@
                if ((switchPower = device.findService(serviceId)) != null) {
                    System.out.println("Service disappeared: " + switchPower);
                    if (device.getType().equals(new UDADeviceType("Alarm"))) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                controller.changeAlarmTextOff();
                            }
                        });
                    }
                }
            }

//            @Override
//            public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
//                super.remoteDeviceUpdated(registry, device);
//                System.out.println(
//                        "Remote device updated: " + device.getDisplayString()
//                );
//            }

            @Override
            public void beforeShutdown(Registry registry) {
                super.beforeShutdown(registry);
                System.out.println("Before shutdown, the registry has devices:" + registry.getDevices().size());
            }

            @Override
            public void afterShutdown() {
                super.afterShutdown();
                System.out.println("Shutdown of registry complete!");
            }
        };
    }

    // DOC: REGISTRYLISTENER
    // DOC: EXECUTEACTION

    public static void shutdowService() {
        upnpService.shutdown();
    }

    public static void alarmNotify() {
        for (DeviceManager device : devices) {
            device.setPower(true);
        }
    }

}
