package com.hivemq;

import com.hivemq.avro.CarData;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    public static void main(String[] args) throws Exception {
        final List<CarModel> cars = IntStream.rangeClosed(0, 99)
                .mapToObj(i -> new CarModel())
                .collect(Collectors.toList());
        writeDataSet(cars);
    }

    public static void writeDataSet(List<CarModel> cars) {
        final int iterations = 100;

        try (final CSVPrinter printer = new CSVPrinter(new FileWriter("test.csv"), CSVFormat.DEFAULT)) {
            printHeader(printer);

            for (int i = 0; i < iterations; ++i) {
                final Date timeStamp = Time.from(Instant.now().plusSeconds(10 * i));
                for (CarModel car : cars) {
                    final CarData nextSample = car.nextValue();
                    printer.print(timeStamp.getTime() / 1000);
                    printer.print("car" + (i + 1));

                    printer.print(nextSample.getCoolantTemp());
                    printer.print(nextSample.getIntakeAirTemp());
                    printer.print(nextSample.getIntakeAirFlowSpeed());
                    printer.print(nextSample.getBatteryPercentage());
                    printer.print(nextSample.getBatteryVoltage());
                    printer.print(nextSample.getCurrentDraw());
                    printer.print(nextSample.getSpeed());
                    printer.print(nextSample.getEngineVibrationAmplitude());
                    printer.print(nextSample.getThrottlePos());
                    printer.print(nextSample.getTirePressure11());
                    printer.print(nextSample.getTirePressure12());
                    printer.print(nextSample.getTirePressure21());
                    printer.print(nextSample.getTirePressure22());
                    printer.print(nextSample.getAccelerometer11Value());
                    printer.print(nextSample.getAccelerometer12Value());
                    printer.print(nextSample.getAccelerometer21Value());
                    printer.print(nextSample.getAccelerometer22Value());
                    printer.print(nextSample.getControlUnitFirmware());
                    printer.println();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printHeader(CSVPrinter printer) throws IOException {
        printer.print("time");
        printer.print("car");
        printer.print("coolant_temp");
        printer.print("intake_air_temp");
        printer.print("intake_air_flow_speed");
        printer.print("battery_percentage");
        printer.print("battery_voltage");
        printer.print("current_draw");
        printer.print("speed");
        printer.print("engine_vibration_amplitude");
        printer.print("throttle_pos");
        printer.print("tire_pressure_1_1");
        printer.print("tire_pressure_1_2");
        printer.print("tire_pressure_2_1");
        printer.print("tire_pressure_2_2");
        printer.print("accelerometer_1_1_value");
        printer.print("accelerometer_1_2_value");
        printer.print("accelerometer_2_1_value");
        printer.print("accelerometer_2_2_value");
        printer.print("control_unit_firmware");
        printer.println();
    }
}
