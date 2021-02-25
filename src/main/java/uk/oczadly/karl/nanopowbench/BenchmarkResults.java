package uk.oczadly.karl.nanopowbench;

/**
 * @author Karl Oczadly
 */
public class BenchmarkResults {
    
    private final long timeElapsed, workTime, sols;
    
    public BenchmarkResults(long timeElapsed, long workTime, long sols) {
        this.timeElapsed = timeElapsed;
        this.workTime = workTime;
        this.sols = sols;
    }
    
    
    public long getTotalTimeElapsed() {
        return timeElapsed;
    }
    
    public long getWorkTime() {
        return workTime;
    }
    
    public long getSolutions() {
        return sols;
    }
    
}
