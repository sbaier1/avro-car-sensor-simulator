package com.hivemq;

import com.hivemq.avro.CarData;
import org.apache.commons.lang3.RandomUtils;

/**
 * Models the physical properties and sensor values of an electric vehicle, including synthetic failure modes.
 */
public class CarModel {

    // Typical upper soft speed limit of an EV (140km/h = 38.889m/s)
    private static final float MAX_SPEED = 38.889F;

    // Static inertia for more simplicity, also calculated linearly for more simplicity
    private static final float COOLANT_INERTIA = 0.8F;
    private static final float VEHICLE_INERTIA = 0.8F;

    private static final int AIR_SPEED_MULTIPLIER = 4;

    // Typical battery voltage is somewhere between 200-260V
    private static final int DISCHARGED_BATTERY_VOLTAGE = 180;
    private static final float FULLY_CHARGED_BATTERY_VOLTAGE = 260F;
    private static final float VIBRATION_AMPLITUDE_MULTIPLIER = 100F;

    // Probability of running into a bump on the road in percent
    private static final float BUMP_PROBABILITY = 5F;

    private CarData previousSample;

    public CarData nextValue() {
        // Time between samples in seconds, this will have to be replaced by rate in the device simulator
        final int timeStep = 5;

        final float previousSpeed;
        final float previousThrottlePos;
        final float intakeAirTemp;
        final float prevBatteryPercentage;
        final float batteryVoltage;
        final float prevIntakeAirSpeed;
        if (previousSample == null) {
            // (we assume that the time series starts at any point in time during a trip for now)
            previousSpeed = RandomUtils.nextFloat(0, 50);

            // Much of the data depends on the throttle position,
            // so we generate a random throttle position first
            previousThrottlePos = RandomUtils.nextFloat(0, 1);
            intakeAirTemp = RandomUtils.nextFloat(15, 40);
            prevBatteryPercentage = RandomUtils.nextFloat(0.3F, 1.0F);
            batteryVoltage = DISCHARGED_BATTERY_VOLTAGE +
                    prevBatteryPercentage * (FULLY_CHARGED_BATTERY_VOLTAGE - DISCHARGED_BATTERY_VOLTAGE);
            // We'll assume we're heading straight forward the whole time
            prevIntakeAirSpeed = previousSpeed * AIR_SPEED_MULTIPLIER;
        } else {
            previousSpeed = previousSample.getSpeed();
            previousThrottlePos = previousSample.getThrottlePos();
            intakeAirTemp = previousSample.getIntakeAirTemp();

            prevBatteryPercentage = previousSample.getBatteryPercentage();
            batteryVoltage = previousSample.getBatteryVoltage();
            prevIntakeAirSpeed = previousSample.getIntakeAirFlowSpeed();
        }

        // The throttle position translates almost directly to current drawn in this model
        final float currentDraw = previousThrottlePos * (Math.abs(260 - batteryVoltage) + 5);
        // We will assume that there is no lag between the coolant temp sensor picking up the change
        // in temp from higher current between cells and engine
        final float previousCoolantTemp = RandomUtils.nextFloat(intakeAirTemp, intakeAirTemp + 20);
        final float coolantTemp = COOLANT_INERTIA * previousCoolantTemp + (1 - COOLANT_INERTIA) * currentDraw * 0.5F;

        // Instantaneous acceleration and deceleration with "recuperation" for simplicity
        final float speed = VEHICLE_INERTIA * previousSpeed + (1 - VEHICLE_INERTIA) * (previousThrottlePos * MAX_SPEED);

        // Engine shaft rotation translates directly to the vibration amplitude
        final float engineVibrationAmplitude = speed * VIBRATION_AMPLITUDE_MULTIPLIER;

        final int tirePressure1 = RandomUtils.nextInt(30, 35);
        final int tirePressure2 = RandomUtils.nextInt(30, 35);
        final int tirePressure3 = RandomUtils.nextInt(30, 35);
        final int tirePressure4 = RandomUtils.nextInt(30, 35);

        final float randomFactor = RandomUtils.nextFloat(0, 100);
        final float accelerometerValue1 = randomFactor < BUMP_PROBABILITY ?
                RandomUtils.nextFloat(2, 3) :
                RandomUtils.nextFloat(0, 1);
        final float accelerometerValue2 = randomFactor < BUMP_PROBABILITY ?
                RandomUtils.nextFloat(2, 3) :
                RandomUtils.nextFloat(0, 1);
        final float accelerometerValue3 = randomFactor < BUMP_PROBABILITY ?
                RandomUtils.nextFloat(2, 3) :
                RandomUtils.nextFloat(0, 1);
        final float accelerometerValue4 = randomFactor < BUMP_PROBABILITY ?
                RandomUtils.nextFloat(2, 3) :
                RandomUtils.nextFloat(0, 1);

        final int firmwareVersion = 123;
        final CarData nextModel = CarData.newBuilder()
                .setThrottlePos(previousThrottlePos)
                .setIntakeAirFlowSpeed(prevIntakeAirSpeed)
                .setIntakeAirTemp(intakeAirTemp)
                .setSpeed(speed)
                .setEngineVibrationAmplitude(engineVibrationAmplitude)
                // Accelerometer
                .setAccelerometer11Value(accelerometerValue1)
                .setAccelerometer12Value(accelerometerValue2)
                .setAccelerometer13Value(accelerometerValue3)
                .setAccelerometer14Value(accelerometerValue4)
                // Tire pressure
                .setTirePressure11(tirePressure1)
                .setTirePressure12(tirePressure2)
                .setTirePressure21(tirePressure3)
                .setTirePressure22(tirePressure4)
                // Battery
                .setBatteryPercentage(prevBatteryPercentage)
                .setBatteryVoltage(batteryVoltage)
                .setCoolantTemp(coolantTemp)
                .setCurrentDraw(currentDraw)
                .setControlUnitFirmware(firmwareVersion)
                .build();
        previousSample = nextModel;
        return nextModel;
    }

    /**
     * Check if a random event is going to happen based on its percentage
     * @param percentage evenly distributed probability of the event
     * @return {@code true} if the event happens, {@code false} otherwise
     */
    private boolean eventHappens(final float percentage) {
        return percentage < RandomUtils.nextFloat(0, 100);
    }
}
