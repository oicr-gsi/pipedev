package ca.on.oicr.pde.utilities;

import org.apache.commons.lang3.time.StopWatch;

public class Timer {

    private StopWatch stopWatch;

    private Timer() {
        stopWatch = new StopWatch();
    }

    public static Timer start() {
        Timer timer = new Timer();
        timer.stopWatch.start();
        return timer;
    }

    public String stop() {
        stopWatch.stop();
        return stopWatch.toString();
    }
}
