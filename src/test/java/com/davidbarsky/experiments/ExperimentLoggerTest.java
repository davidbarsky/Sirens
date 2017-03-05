package com.davidbarsky.experiments;

import com.davidbarsky.dag.models.states.MachineType;
import com.davidbarsky.schedulers.EdgeZero;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.*;

public class ExperimentLoggerTest {

    @Test
    public void toCSV() throws Exception {
        List<ExperimentResult> experimentResults = ExperimentRunner.runSeries(new EdgeZero(), 10, 12, MachineType.SMALL);
        String csv = ExperimentLogger.toCSV(experimentResults);

        // There's probably a better way to test formatting. Cost is non-deterministic
        System.out.println(csv);
        assertNotNull(csv);
    }

    @Test
    public void writeToFile() throws Exception {
        List<ExperimentResult> experimentResults = ExperimentRunner.runSeries(new EdgeZero(), 10, 12, MachineType.SMALL);
        String testCSV = ExperimentLogger.toCSV(experimentResults);

        File file = File.createTempFile("output", "txt");
        file.deleteOnExit();
        ExperimentLogger.writeToFile(experimentResults, file);

        String testActual = String.join("\n", Files.readAllLines(file.toPath())) + "\n";
        assertEquals(testCSV, testActual);
    }
}