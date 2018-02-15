package com.bid.car;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Timer;
import java.util.TimerTask;

public class ClockTask {

    private Logger logger = LoggerFactory.getLogger(ClockTask.class);

    //TODO 每天执行一次
    public void execute(Runnable runnable, LocalTime localTime) {
        Timer timer = new Timer();
        long delay = Duration.between(LocalTime.now(), localTime).toMillis();
        logger.info("delay:{}", delay);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runnable.run();
            }
        }, delay, Duration.ofDays(1).toMillis());
    }
}
