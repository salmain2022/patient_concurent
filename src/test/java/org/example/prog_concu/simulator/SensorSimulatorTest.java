package org.example.prog_concu.simulator;

import org.example.prog_concu.entities.SensorData;
import org.junit.jupiter.api.Test;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class SensorSimulatorTest {

    @Test
    public void testSensorSimulatorRuns() throws InterruptedException {
        Map<Long, SensorData> sensorDataMap = new ConcurrentHashMap<>();
        SensorSimulator simulator = new SensorSimulator(1L, 3000, sensorDataMap);
        Thread thread = new Thread(simulator);
        thread.start();

        Thread.sleep(10000);
        simulator.stop();
        thread.join();

        assert(sensorDataMap.containsKey(1L));
    }
}
// This test starts the SensorSimulator in a separate thread, lets it run for 10 seconds,
// and then stops it. It checks that the sensor data for the patient with ID 1 is present in the map after the simulation.
// The test uses JUnit 5 annotations and assertions to validate the behavior of the SensorSimulator.
// Note: Ensure that you have the necessary dependencies for JUnit 5 in your project to run this test.
