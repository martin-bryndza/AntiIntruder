/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.trayapp;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Bato
 */
public class Main {

    private final static Logger log = LoggerFactory.getLogger(Main.class);
    private static WorkstationLockListener workstationLockListener = null;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException | ClassNotFoundException ex) {
            log.error(ex.getMessage());
        }
        Thread t = new Thread(new WorkstationLockListenerRunner());
        t.start();
        TrayIconManager.initialize();
        StateCheckService.getInstance().start();
        SwingUtilities.invokeLater(() -> {
        });
    }

    static void programFinish() {
        if (workstationLockListener != null) {
            workstationLockListener.destroy();
        }
        TrayIconManager tim = TrayIconManager.getInstance();
        if (tim != null) {
            tim.close();
        }
        System.exit(0);
    }

    private static class WorkstationLockListenerRunner implements Runnable {

        @Override
        public void run() {
            workstationLockListener = new WorkstationLockListener();
        }

    }

}
