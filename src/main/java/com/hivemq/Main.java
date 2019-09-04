package com.hivemq;

import com.hivemq.avro.CarData;
import org.apache.avro.message.BinaryMessageEncoder;

public class Main {

    public static void main(String[] args) throws Exception {
        final BinaryMessageEncoder<CarData> encoder = CarData.getEncoder();
        final CarModel carModel = new CarModel();
        final CarData carData = carModel.nextValue();
        encoder.encode(carData);
    }
}
