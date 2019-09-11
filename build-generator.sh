#!/usr/bin/env bash

./gradlew shadowJar
docker build -f Dockerfile -t load-generator build/libs/