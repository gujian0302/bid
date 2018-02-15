package com.bid.car;

import org.junit.Test;

import java.time.LocalTime;

public class ClockTaskTest {

    @Test
    public void testRun(){
        ClockTask clockTask = new ClockTask();
        LocalTime localTime = LocalTime.of(13, 35, 50);
        clockTask.execute(() -> {
            System.out.print("success");
        }, localTime);
    }
}
