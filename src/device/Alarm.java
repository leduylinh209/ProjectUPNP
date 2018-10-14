/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package device;

import img.AutoResizeIcon;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.*;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.model.types.UDN;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Admins
 */
public class Alarm extends JFrame implements PropertyChangeListener, ActionListener{
    private UpnpService upnpService;
    private UDN udn = new UDN(UUID.randomUUID());
    private JButton but;
    private JLabel label;
    
    public Alarm()
    {
        init();
        onCreate();
    }
    
    private void init()
    {
        setLayout(null); setSize(400, 430); setTitle(friendlyName);
        setResizable(false); setResizable(false);
        label = new JLabel(); label.setBounds(0, 0, 380, 300);
        but = new JButton("ON/OFF"); but.setBounds(50, 305, 300, 50);
        AutoResizeIcon.setIcon(label, "img/alarm.png");
        add(label); add(but); but.addActionListener(this);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        
        time.setBounds(50, 360, 30, 30); add(time);
        JLabel la = new JLabel("Giây"); la.setBounds(85, 360, 30, 30); add(la);
    }
    
    JTextField time = new JTextField();
    
    protected LocalService<SwitchStatus> getSwitchStatusService()
    {
        if(upnpService == null) return null;
        LocalDevice phoneDevice;
        if((phoneDevice = upnpService.getRegistry().getLocalDevice(udn, true))==null)
            return null;
        return (LocalService<SwitchStatus>)
                phoneDevice.findService(new UDAServiceType("SwitchStatus", 1));
    }
    
    public void onServiceConnection()
    {
        upnpService = new UpnpServiceImpl();
        
        LocalService<SwitchStatus> switchStatusService = getSwitchStatusService();
        if (switchStatusService == null) 
        {
            try {
                    LocalDevice phoneDevice = createDevice();

                    System.out.println("Created device");
                    upnpService.getRegistry().addDevice(phoneDevice);

                    switchStatusService = getSwitchStatusService();

                } catch (Exception ex) {
                    System.out.println("Creating device failed");
                    return;
                }
        }
        
        switchStatusService.getManager().getImplementation().getPropertyChangeSupport()
                .addPropertyChangeListener(this);
    }
    
    protected LocalDevice createDevice() throws org.fourthline.cling.model.ValidationException
    {
        DeviceType type = new UDADeviceType("Alarm", 1);
        
        DeviceDetails details = new DeviceDetails(friendlyName,
                new ManufacturerDetails(manufacturerDetails),
                new ModelDetails("VirtualPhone", "A phone with 2 state ringing and not ringing", "v1"));
        
        LocalService service = new AnnotationLocalServiceBinder().read(SwitchStatus.class);
        service.setManager(new DefaultServiceManager<>(service, SwitchStatus.class));
        
        LocalDevice device = new LocalDevice(new DeviceIdentity(udn), type, details, createDefaultDeviceIcon(), service);
        return device;
    }
    
    final String friendlyName = "Alarm";
    final String manufacturerDetails = "Rooster";
    
    private void onCreate()
    {
        onServiceConnection();
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        if (pce.getPropertyName().equals("status")) {
            System.out.println("Property Changed");
            boolean status = getStatus();
            if(status) AutoResizeIcon.setIcon(label, "img/alarmon.png");
            else AutoResizeIcon.setIcon(label, "img/alarm.png");
            repaint();
            System.out.println(!status+" -> "+status);
        }
    }
    
    protected Icon createDefaultDeviceIcon() {
        try {
            File file = new File("src/img/Webp.net-resizeimage.jpg");
            return new Icon("image/jpg", 48, 48, 8, file);
        } catch (IOException ex) {
            Logger.getLogger(Alarm.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public static void main(String[] args) {
        new Alarm();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        boolean status = getStatus();
        if(runtime)
        {
            x.stop();
            runtime = false;
            time.setEditable(true);
            return;
        }
        if(status == true) 
        {
            if(x!=null)
            {
                x.stop();
                time.setEditable(true);
            }
            switchStatus();
            time.setEditable(true);
            return;
        }
        try {
            time.setEditable(!time.isEditable());
            int t = Integer.parseInt(time.getText());
            x = new Thread(new AlarmAction(this, t));
            x.start();
            runtime = true;
        } catch (Exception ex) {
            time.setEditable(true);
        }
    }
    
    public boolean runtime = false;
    Thread x;
    public void updateTime(int t)
    {
        time.setText(""+t);
    }
    
    public void switchStatus()
    {
        boolean status = !getStatus();
        Service service = getSwitchStatusService();
        Action action = service.getAction("SetTarget");
        ActionInvocation invocation = new ActionInvocation(action);
        invocation.setInput("NewTargetValue", status);
        new ActionCallback.Default(invocation, upnpService.getControlPoint()).run();
    }
    
    public boolean getStatus()
    {
        Action action = getSwitchStatusService().getAction("GetStatus");
        ActionInvocation invocation = new ActionInvocation(action);
        new ActionCallback.Default(invocation, upnpService.getControlPoint()).run();
        Boolean status = (Boolean) invocation.getOutput("ResultStatus").getValue();
        boolean value = status.booleanValue();
        return value;
    }
    
    // Close window and destroy upnp service
    private void formWindowClosing(java.awt.event.WindowEvent evt)
    {
        System.out.println("Destroyed device");
        onDestroy();
    }
    
    private void onDestroy()
    {
        LocalService<SwitchStatus> switchStatusService = getSwitchStatusService();
        LocalDevice device = upnpService.getRegistry().getLocalDevice(udn, true);
        if (switchStatusService != null)
            switchStatusService.getManager().getImplementation().getPropertyChangeSupport()
                .removePropertyChangeListener(this);
        if(device != null)
            upnpService.getRegistry().removeDevice(device);
        upnpService.shutdown();
    }
}
