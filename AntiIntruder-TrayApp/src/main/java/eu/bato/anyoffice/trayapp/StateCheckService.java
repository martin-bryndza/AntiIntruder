package eu.bato.anyoffice.trayapp;

import eu.bato.anyoffice.trayapp.config.Configuration;
import eu.bato.anyoffice.trayapp.config.Property;
import java.awt.MouseInfo;
import java.awt.Point;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Schedules tasks for checking current server state and updating interface and
 * also for mouse tracking. The state check tasks ping the server and call
 TrayIconManager for updateFromServer. The mouse tracking task sends lock to server if
 the mouse hasn't moved for 2 minutes. Sends unlock if the mouse moves again.
 *
 * @author Bato
 */
class StateCheckService {

    final static Logger log = LoggerFactory.getLogger(StateCheckService.class);

    private static StateCheckService instance;

    private StateCheckService() {

    }

    static StateCheckService getInstance() {
        if (instance == null) {
            instance = new StateCheckService();
        }
        return instance;
    }

    void start() {
        ScheduledExecutorService scheduledPool = Executors.newScheduledThreadPool(2);

        //the state checking task
        Long period = Configuration.getInstance().getLongProperty(Property.CHECK_INTERVAL);
        ScheduledFuture sf = null;
        if (period <= 0) {
            log.info("StateCheckTask is disabled.");
        } else {
            sf = scheduledPool.scheduleWithFixedDelay(new StateCheckTask(), period, period, TimeUnit.MILLISECONDS);
        }
        final ScheduledFuture stateCheckFuture = sf;

        // mouse tracking task
        final ScheduledFuture mousePositionFuture = scheduledPool.scheduleWithFixedDelay(new MousePositionTask(), 10000, 5000, TimeUnit.MILLISECONDS);

        // these threads should be scheduled on forever > in case they fail, stop both of them and run them again
        Runnable watchdog = new Runnable() {

            @Override
            public void run() {
                while (true) {
                    try {
                        stateCheckFuture.get();
                        mousePositionFuture.get();
                    } catch (ExecutionException | InterruptedException e) {
                        log.warn("Some of the scheduled tasks threw exception.", e);
                        stateCheckFuture.cancel(false);
                        mousePositionFuture.cancel(true);
                        log.info("Tasks will restart...");
                        start();
                        return;
                    }
                }
            }
        };
        new Thread(watchdog).start();
    }

    private class StateCheckTask implements Runnable {

        @Override
        public void run() {
            log.debug("States check started...");
            TrayIconManager.getInstance().pingServer();
            TrayIconManager.getInstance().updateFromServer();
            if (!RestClient.isServerOnline()) {
                log.error("Conection to server failed.");
                try {
                    Thread.sleep(20000);
                } catch (InterruptedException ex) {
                    //does not matter
                }
            }
            log.info("States check finished.");
        }
    }

    private class MousePositionTask implements Runnable {

        private Point last = new Point();
        private boolean locked = false;
        private int toLock = 0;

        @Override
        public void run() {
            Point p = MouseInfo.getPointerInfo().getLocation();
            log.debug("Mouse position is {},{} and last was {},{}", p.getX(), p.getY(), last.getX(), last.getY());
            if (p.equals(last) && !locked && !TrayIconManager.getInstance().getCurrentState().isAwayState()
                    && 24 <= toLock++) { // the mouse position has to be the same for 24 times
                log.info("Sending session lock due to mouse not being moved.");
                TrayIconManager.getInstance().lock(true);
                locked = true;
                toLock = 0;
            } else if (!p.equals(last)) {
                toLock = 0;
                if (locked) {
                    log.info("Mouse moved, sending session unlock");
                    TrayIconManager.getInstance().lock(false);
                    locked = false;
                }
            }
            last = p;
        }

    }

}
