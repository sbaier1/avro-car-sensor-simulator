package com.hivemq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.avro.CarData;
import com.hivemq.simulator.load.generators.PluginPayloadGenerator;
import com.hivemq.simulator.load.generators.PluginPayloadGeneratorInput;
import com.hivemq.util.IgnoreSchemaProperty;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class CarDataPayloadGenerator implements PluginPayloadGenerator {
    private static final @NotNull Logger log = LoggerFactory.getLogger(CarDataPayloadGenerator.class);

    private static final @NotNull ByteBuffer ERROR_PAYLOAD = ByteBuffer.wrap("ERROR".getBytes());

    private static final @NotNull ObjectMapper objectMapper = new ObjectMapper();

    // Unfortunately no way around it at the moment, no DI in device simulator plugins yet
    static {
        objectMapper.addMixIn(CarData.class, IgnoreSchemaProperty.class);
    }

    private final @NotNull CarModel model;

    public CarDataPayloadGenerator() {
        this.model = new CarModel();
    }

    @Override
    public @NotNull ByteBuffer nextPayload(@NotNull PluginPayloadGeneratorInput pluginPayloadGeneratorInput) {
        final CarData value = model.nextValue();
        try {
            return ByteBuffer.wrap(objectMapper.writeValueAsBytes(value));
        } catch (JsonProcessingException e) {
            log.error("Could not write JSON for object {}", value.toString(), e);
        }
        return ERROR_PAYLOAD;
    }
}
