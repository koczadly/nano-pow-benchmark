package uk.oczadly.karl.nanopowbench.benchmark;

/**
 * @author Karl Oczadly
 */
public class BenchmarkResults {
    
    private final long timeElapsedNs, workTimeNs, totalHashes, iterations;
    
    public BenchmarkResults(long timeElapsedNs, long workTimeNs, long totalHashes, long iterations) {
        this.timeElapsedNs = timeElapsedNs;
        this.workTimeNs = workTimeNs;
        this.totalHashes = totalHashes;
        this.iterations = iterations;
    }
    
    
    public long getTimeElapsed() {
        return timeElapsedNs;
    }
    
    public long getWorkTime() {
        return workTimeNs;
    }
    
    public long getTotalHashes() {
        return totalHashes;
    }
    
    public long getIterations() {
        return iterations;
    }
    
}
