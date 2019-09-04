# Car Data simulation

Simulates an electric vehicle's sensor data over time, with synthetic failure modes.
See the Avro [schema](src/main/resources/cardata-v1.avsc) for more info on the data stored in a sample.

## Failure modes

For probabilities of events and implementation details, see [CarModel](src/main/java/com/hivemq/CarModel.java)

### Shock failure/degradation

The car's "shock performance" is measured by accelerometers on the chassis at every corner of the vehicle.
In the event of a bump, minor acceleration takes place.

With a certain probability, the shock of a car might fail, in which case, the default acceleration at each iteration increases slightly and the acceleration in case of a bump is much larger.

We assume that the acceleration data here is not a simple point in time measurement, but an aggregation over time between samples.

### Tire pressure loss

The car might lose tire pressure with a certain probability. In this case, the pressure will drop below the assumed nominal range of `[30..35]`

### Engine/Drive shaft degradation

The car has vibration sensors which monitor the vibration of the engine.

With a certain probability, a car's vibration might increase greatly, indicating that some sort of mechanical degradation or failure is taking place. 

### Overheating

The car has a coolant temperature sensor. The fictional (liquid) coolant in this model is used for engine and battery cells.

With a certain probability, a car's coolant temperature might increase greatly (instead of linearly with current draw), indicating battery cell failure , short circuit or other electrical failure.

### Battery cell degradation

The rate of decrease of the battery's percentage might increase greatly, indicating battery cell degradation.

### Outdated firmware

Firmware version is indicated in the model, a single car might use an outdated software version.