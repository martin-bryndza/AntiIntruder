package eu.bato.anyoffice.trayapp;

import eu.bato.anyoffice.trayapp.config.Configuration;
import eu.bato.anyoffice.trayapp.config.Property;
import java.util.Timer;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
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
        Timer t = new Timer();
        Long period = Configuration.getInstance().getLongProperty(Property.CHECK_INTERVAL);
        if (period <= 0) {
            log.info("Scheduler is disabled.");
        } else {
            t.scheduleAtFixedRate(new StateCheckTask(), period, period);
            log.info("Scheduler service started with period " + period);
        }
    }

    private class StateCheckTask extends TimerTask {

        @Override
        public void run() {
            log.debug("States check started...");
            TrayIconManager.getInstance().update();
            log.debug("States check finished.");
        }
    }

}
