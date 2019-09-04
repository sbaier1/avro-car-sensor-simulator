package com.hivemq;

import com.hivemq.avro.CarData;
import org.apache.commons.lang3.RandomUtils;

/**
 * Models the physical properties and sensor values of an electric vehicle, including synthetic failure modes.
 */
public class CarModel {

    // Failure Modes
    private static final float SHOCK_DEGRADATION_PROBABILITY = 1F;
    private static final float TIRE_PRESSURE_LOSS_PROBABILITY = 0.5F;
    private static final float VIBRATION_DRIVE_SHAFT_DEGRADATION_PROBABILITY = 1F;
    private static final float OVERHEATING_PROBABILITY = 0.2F;
    private static final float BATTERY_CELL_DEGRADATION_PROBABILITY = 1F;
    private static final float OUTDATED_FIRMWARE_PROBABILITY = 4F;

    // Typical upper soft speed limit of an EV (140km/h = 38.889m/s)
    private static final float MAX_SPEED = 38.889F;

    // Static inertia for more simplicity, also calculated linearly for more simplicity
    private static final float COOLANT_INERTIA = 0.8F;
    private static final float VEHICLE_INERTIA = 0.8F;

    private static final float AIR_SPEED_MULTIPLIER = 4;
    private static final float VIBRATION_AMPLITUDE_MULTIPLIER = 100F;

    // Typical battery voltage is somewhere between 200-260V
    private static final float DISCHARGED_BATTERY_VOLTAGE = 180;
    private static final float FULLY_CHARGED_BATTERY_VOLTAGE = 260F;

    // Probability of running into a bump on the road in percent
    private static final float BUMP_PROBABILITY = 5F;

    private CarData previousSample;

    /* Failure mode state */
    private boolean pressureLossTire1;
    private boolean pressureLossTire2;
    private boolean pressureLossTire3;
    private boolean pressureLossTire4;

    private boolean shockFailure1;
    private boolean shockFailure2;
    private boolean shockFailure3;
    private boolean shockFailure4;

    private boolean driveShaftDegradation;

    private boolean overheatingCoolant;

    private boolean outdatedFirmware;

    public CarData nextValue() {
        // Time between samples in seconds, this will have to be replaced by rate in the device simulator
        // TODO in addition to inertia, we can use this to calculate changes over time, e.g. throttle is pushed harder -> speed will increase until next iteration
        final int timeStep = 5;

        final float previousSpeed;
        final float previousThrottlePos;
        final float intakeAirTemp;
        final float prevBatteryPercentage;
        final float batteryVoltage;
        final float prevIntakeAirSpeed;
        final float previousCoolantTemp;
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

            previousCoolantTemp = RandomUtils.nextFloat(intakeAirTemp, intakeAirTemp + 20);
        } else {
            previousSpeed = previousSample.getSpeed();
            previousThrottlePos = previousSample.getThrottlePos();
            intakeAirTemp = previousSample.getIntakeAirTemp();

            prevBatteryPercentage = previousSample.getBatteryPercentage() - previousSpeed * 0.008F;
            batteryVoltage = previousSample.getBatteryVoltage();
            prevIntakeAirSpeed = previousSample.getIntakeAirFlowSpeed();

            previousCoolantTemp = previousSample.getCoolantTemp() + previousSpeed * 0.008F;
        }

        // Update state of failure modes. Once an event happens the state is kept across iterations
        this.pressureLossTire1 = pressureLossTire1 || eventHappens(TIRE_PRESSURE_LOSS_PROBABILITY);
        this.pressureLossTire2 = pressureLossTire2 || eventHappens(TIRE_PRESSURE_LOSS_PROBABILITY);
        this.pressureLossTire3 = pressureLossTire3 || eventHappens(TIRE_PRESSURE_LOSS_PROBABILITY);
        this.pressureLossTire4 = pressureLossTire4 || eventHappens(TIRE_PRESSURE_LOSS_PROBABILITY);

        this.shockFailure1 = shockFailure1 || eventHappens(SHOCK_DEGRADATION_PROBABILITY);
        this.shockFailure2 = shockFailure2 || eventHappens(SHOCK_DEGRADATION_PROBABILITY);
        this.shockFailure3 = shockFailure3 || eventHappens(SHOCK_DEGRADATION_PROBABILITY);
        this.shockFailure4 = shockFailure4 || eventHappens(SHOCK_DEGRADATION_PROBABILITY);

        this.driveShaftDegradation = driveShaftDegradation || eventHappens(VIBRATION_DRIVE_SHAFT_DEGRADATION_PROBABILITY);
        this.overheatingCoolant = overheatingCoolant || eventHappens(OVERHEATING_PROBABILITY);
        this.outdatedFirmware = outdatedFirmware || eventHappens(OUTDATED_FIRMWARE_PROBABILITY);

        // The throttle position translates almost directly to current drawn in this model (and also relates to the battery voltage)
        final float currentDraw = previousThrottlePos * (Math.abs(260 - batteryVoltage) + 5);

        // We will assume that there is no lag between the coolant temp sensor picking up the change
        // in temp from higher current between cells and engine
        final float coolantTemp = overheatingCoolant ?
                COOLANT_INERTIA * previousCoolantTemp + ((1 - COOLANT_INERTIA) * (previousCoolantTemp + currentDraw * 2.5F))
                : COOLANT_INERTIA * previousCoolantTemp + (1 - COOLANT_INERTIA) * (previousCoolantTemp + currentDraw * 0.5F);

        // Instantaneous acceleration and deceleration with "recuperation" for simplicity
        final float speed = VEHICLE_INERTIA * previousSpeed + (1 - VEHICLE_INERTIA) * (previousThrottlePos * MAX_SPEED);

        // Drive shaft rotation translates directly to the vibration amplitude
        final float engineVibrationAmplitude = driveShaftDegradation ?
                speed * (VIBRATION_AMPLITUDE_MULTIPLIER * 1.5F)
                : speed * VIBRATION_AMPLITUDE_MULTIPLIER;

        final int tirePressure1 = pressureLossTire1 ? RandomUtils.nextInt(20, 25) : RandomUtils.nextInt(30, 35);
        final int tirePressure2 = pressureLossTire2 ? RandomUtils.nextInt(20, 25) : RandomUtils.nextInt(30, 35);
        final int tirePressure3 = pressureLossTire3 ? RandomUtils.nextInt(20, 25) : RandomUtils.nextInt(30, 35);
        final int tirePressure4 = pressureLossTire4 ? RandomUtils.nextInt(20, 25) : RandomUtils.nextInt(30, 35);

        final boolean bumpHappens = eventHappens(BUMP_PROBABILITY);
        final float accelerometerValue1 = getShockAcceleration(shockFailure1, bumpHappens);
        final float accelerometerValue2 = getShockAcceleration(shockFailure2, bumpHappens);
        final float accelerometerValue3 = getShockAcceleration(shockFailure3, bumpHappens);
        final float accelerometerValue4 = getShockAcceleration(shockFailure4, bumpHappens);

        // Well, this one is kind of lame
        final int firmwareVersion = outdatedFirmware ?
                1000
                : 2000;
        final CarData nextModel = CarData.newBuilder()
                .setThrottlePos(previousThrottlePos)
                .setIntakeAirFlowSpeed(prevIntakeAirSpeed)
                .setIntakeAirTemp(intakeAirTemp)
                .setSpeed(speed)
                .setEngineVibrationAmplitude(engineVibrationAmplitude)
                // Accelerometer
                .setAccelerometer11Value(accelerometerValue1)
                .setAccelerometer12Value(accelerometerValue2)
                .setAccelerometer21Value(accelerometerValue3)
                .setAccelerometer22Value(accelerometerValue4)
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

    private float getShockAcceleration(boolean shockFailed, boolean bumpHappens) {
        if (bumpHappens) {
            if (shockFailed) {
                return RandomUtils.nextFloat(5, 7);
            } else {
                return RandomUtils.nextFloat(2, 3);
            }
        } else {
            if (shockFailed) {
                return RandomUtils.nextFloat(3, 4);
            } else {
                return RandomUtils.nextFloat(0, 1);
            }
        }
    }

    /**
     * Check if a random event is going to happen based on its percentage
     *
     * @param percentage evenly distributed probability of the event
     * @return {@code true} if the event happens, {@code false} otherwise
     */
    private boolean eventHappens(final float percentage) {
        System.out.println("Anomaly occured");
        return percentage > RandomUtils.nextFloat(0, 100);
    }
}
