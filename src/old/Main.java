package old;

/*
 * Copyright (C) 2013 4th Line GmbH, Switzerland
 *
 * The contents of this file are subject to the terms of either the GNU
 * Lesser General Public License Version 2 or later ("LGPL") or the
 * Common Development and Distribution License Version 1 or later
 * ("CDDL") (collectively, the "License"). You may not use this file
 * except in compliance with the License. See LICENSE.txt for more
 * information.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.controlpoint.SubscriptionCallback;
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.header.UDNHeader;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.state.StateVariableValue;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;

import java.util.ArrayList;
import java.util.Map;


/**
 * Runs a simple UPnP discovery procedure.
 */
public class Main {

    public static UpnpService upnpService;
    public static ArrayList<ServiceManager> volume_services = new ArrayList<>();

    public static void alarm_notify(boolean status) {
        for (ServiceManager serviceManager : volume_services) {
            serviceManager.scenario_started();
        }
    }

    public static int notify = 2;
    public static Setup setup = new Setup();

    public static void main(String[] args) throws Exception {

        setup.setVisible(true);
        System.out.println(setup.getOption());
        RegistryListener listener = new RegistryListener() {

            public void remoteDeviceDiscoveryStarted(Registry registry,
                                                     RemoteDevice device) {
                if (device.isFullyHydrated() && (device.getType().equals(new UDADeviceType("MusicPlayer"))
                        || device.getType().equals(new UDADeviceType("Curtain"))
                        || device.getType().equals(new UDADeviceType("CoffeeMaker")))) {
                    Service power_service = device.findService(new UDAServiceType("SwitchStatus"));
                    volume_services.add(new ServiceManager(power_service, upnpService));
                } else if (device.isFullyHydrated() && device.getType().equals(new UDADeviceType("Alarm"))) {
                    Service service = device.findService(new UDAServiceType("SwitchStatus"));
                    SubscriptionCallback alarmCallback = new SubscriptionCallback(service) {
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
                            final int status_value = Integer.parseInt(status.toString());
                            System.out.println(setup.getOption());
                            alarm_notify(status_value == 1);
                        }

                        ;

                        @Override
                        protected void eventsMissed(GENASubscription subscription, int numberOfMissedEvents) {

                        }
                    };
                    upnpService.getControlPoint().execute(alarmCallback);
                }
            }

            public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
//                System.out.println(
//                        "Discovery failed: " + device.getDisplayString() + " => " + ex
//                );
            }

            public void remoteDeviceAdded(Registry registry, final RemoteDevice device) {
//                System.out.println(
//                        "Remote device available: " + device.getDisplayString()
//                );
                if (device.isFullyHydrated() && (device.getType().equals(new UDADeviceType("MusicPlayer"))
                        || device.getType().equals(new UDADeviceType("Curtain"))
                        || device.getType().equals(new UDADeviceType("CoffeeMaker")))) {
                    Service power_service = device.findService(new UDAServiceType("SwitchStatus"));
                    volume_services.add(new ServiceManager(power_service, upnpService));
                }
//                System.out.println(device.getType().getDisplayString());
//                System.out.print(device.getIdentity().getUdn());
                else if (device.isFullyHydrated() && device.getType().equals(new UDADeviceType("Alarm"))) {
                    Service service = device.findService(new UDAServiceType("SwitchStatus"));
                    SubscriptionCallback alarmCallback = new SubscriptionCallback(service) {
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
                            final int status_value = Integer.parseInt(status.toString());
                            if (status_value < notify) {
                                if (setup.getOption() == Setup.WAKE_UP)
                                    alarm_notify(status_value == 1);
                            }
                            notify = status_value;

                        }

                        ;

                        @Override
                        protected void eventsMissed(GENASubscription subscription, int numberOfMissedEvents) {

                        }
                    };
                    upnpService.getControlPoint().execute(alarmCallback);
                }
            }

            public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
                System.out.println(
                        "Remote device updated: " + device.getDisplayString()
                );
            }

            public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
                System.out.println(
                        "Remote device removed: " + device.getDisplayString()
                );
            }

            public void localDeviceAdded(Registry registry, LocalDevice device) {
                System.out.println(
                        "Local device added: " + device.getDisplayString()
                );
            }

            public void localDeviceRemoved(Registry registry, LocalDevice device) {
                System.out.println(
                        "Local device removed: " + device.getDisplayString()
                );
            }

            public void beforeShutdown(Registry registry) {
                System.out.println(
                        "Before shutdown, the registry has devices: " + registry.getDevices().size()
                );
            }

            public void afterShutdown() {
                System.out.println("Shutdown of registry complete!");

            }
        };

        // This will create necessary network resources for UPnP right away
        System.out.println("Starting Cling...");
        upnpService = new UpnpServiceImpl(listener);

        // Send a search message to all devices and services, they should respond soon
        System.out.println("Sending SEARCH message to all devices...");
        upnpService.getControlPoint().search(new UDNHeader(new UDN("7a9bdbde-6c03-4985-ba9c-a3d08c447275")));

        // Let's wait 10 seconds for them to respond
        System.out.println("Waiting 10 seconds before shutting down...");
//        Thread.sleep(10000);
        while (true) ;
    }
}


