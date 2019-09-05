#!/usr/bin/env bash

set -eo pipefail

./gradlew shadowJar
java -jar build/libs/avro-payloads-1.0-SNAPSHOT-all.jar $@
