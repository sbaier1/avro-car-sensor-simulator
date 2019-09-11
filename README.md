# Car Data simulation

Simulates an electric vehicle's sensor data over time, with synthetic failure modes.
See the Avro [schema](src/main/resources/cardata-v1.avsc) for more info on the data stored in a sample.

## Generate test data set

Note: Ensure you are using at least Java 8

`./generate_test_data.sh`

You can also specify the number of data points per car and the number of cars to simulate, e.g.:

`./generate_test_data.sh 50 20`

for `50` data points and `20` cars to simulate.

The file will be generated as `car-sensor-data.csv` in the current directory.

## Build load simulator

The device simulator can simulate this model with multiple models, publishing their data to an MQTT broker with its behavior configured in `scenario.xml`.

You can build the Docker image using `build-generator.sh`. This will add the MQTT payload generator to the device simulator base image.

## Failure modes

For probabilities of events and implementation details, see [CarModel](src/main/java/com/hivemq/CarModel.java)

### Shock failure / degradation

The car's "shock performance" is measured by accelerometers on the chassis at every corner of the vehicle.
In the event of a bump, minor acceleration takes place.

With a certain probability, the shock of a car might fail, in which case, the default acceleration at each iteration increases slightly and the acceleration in case of a bump is much larger.

We assume that the acceleration data here is not a simple point in time measurement, but an aggregation over time.

### Tire pressure loss

The car might lose tire pressure with a certain probability. In this case, the pressure will drop below the assumed nominal range of `[30..35]`psi.

### Engine / Drive shaft degradation

The car has vibration sensors which monitor the vibration of the engine.

With a certain probability, a car's vibration might increase greatly, indicating that some sort of mechanical degradation or failure is taking place. 

### Overheating

The car has a coolant temperature sensor. The fictional (liquid) coolant in this model is used for engine and battery cells.

With a certain probability, a car's coolant temperature might increase greatly (instead of linearly with current draw), indicating battery cell failure , short circuit or other electrical failure.

### Battery cell degradation

The rate of decrease of the battery's percentage might increase greatly, indicating battery cell degradation.

### Outdated firmware

Firmware version is indicated in the model, a single car might use an outdated software version, e.g. due to not being online for a while.