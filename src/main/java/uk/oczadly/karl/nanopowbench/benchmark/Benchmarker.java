package uk.oczadly.karl.nanopowbench.benchmark;

import uk.oczadly.karl.nanopowbench.benchmark.exception.BenchmarkException;

import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

public interface Benchmarker {

    /**
     * Run the benchmark for the specified time duration.
     * @param duration     the preferred running duration of the benchmark
     * @param durationUnit the time unit of time for {@code duration}
     * @return the results of the benchmark
     * @throws BenchmarkException if the benchmark fails
     */
    BenchmarkResults run(long duration, TimeUnit durationUnit) throws BenchmarkException;

    /**
     * @return the benchmark parameters to print
     */
    LinkedHashMap<String, String> getParameters();

}
