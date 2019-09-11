FROM busybox AS builder
RUN mkdir /plugins
COPY avro-payloads-1.0-SNAPSHOT-all.jar /plugins/

FROM sbaier1/device-simulator:latest
COPY --from=builder /plugins /plugins
