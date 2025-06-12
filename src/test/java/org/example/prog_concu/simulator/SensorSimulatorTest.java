package org.example.prog_concu.simulator;

import org.example.prog_concu.entities.SensorData;
import org.example.prog_concu.repository.SensorDataRepository;
import org.example.prog_concu.simulator.SensorSimulator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class SensorSimulatorTest {

    @Test
    public void testSensorSimulatorRuns() throws InterruptedException {
        Map<Long, SensorData> sensorDataMap = new ConcurrentHashMap<>();

        // Mock SensorDataRepository
        SensorDataRepository mockRepository = Mockito.mock(SensorDataRepository.class);

        SensorSimulator simulator = new SensorSimulator(1L, 3000, sensorDataMap, mockRepository);
        Thread thread = new Thread(simulator);
        thread.start();

        Thread.sleep(10000);
        simulator.stop();
        thread.join();

        assert(sensorDataMap.containsKey(1L));
        // Tu peux aussi vérifier que save a été appelé :
        Mockito.verify(mockRepository, Mockito.atLeastOnce()).save(Mockito.any(SensorData.class));
    }
}
