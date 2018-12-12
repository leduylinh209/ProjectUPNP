/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package device;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author anhtu
 */
public class AlarmAction implements Runnable {
    private Alarm alarm;
    private int time;

    public AlarmAction(Alarm alarm, int time) {
        this.alarm = alarm;
        this.time = time;
    }

    @Override
    public void run() {
        while (time > 0) {
            try {
                Thread.sleep(1000);
                time--;
                alarm.updateTime(time);
            } catch (InterruptedException ex) {
                Logger.getLogger(AlarmAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        alarm.switchStatus();
        alarm.runtime = false;
    }
}
