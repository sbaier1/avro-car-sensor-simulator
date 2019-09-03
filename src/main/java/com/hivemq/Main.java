package com.hivemq;

import com.hivemq.avro.CarData;

import java.nio.ByteBuffer;

public class Main {
    public static void main(String[] args) throws Exception {
        final CarData build = CarData.newBuilder()
                .setBatteryPercentage(100)
                .setBatteryVoltage(200)
                .setCurrentDraw(10)
                .setSpeed(100)
                .setIntakeAirTemp(30)
                .setEngineCoolantTemp(90)
                .setIntakeAirFlowSpeed(100)
                // TODO generate data somehow and serialize it
                .build();
        final ByteBuffer encoded = CarData.getEncoder().encode(build);
    }
}
