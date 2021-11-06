package uk.oczadly.karl.nanopowbench.benchmark.opencl.kernel;

import org.jocl.CLException;
import uk.oczadly.karl.nanopowbench.benchmark.exception.BenchmarkInitException;

public interface OCLKernelExecutor {

    void init() throws BenchmarkInitException;

    void configure(long workSize) throws BenchmarkInitException;

    void computeIteration() throws CLException;

    String getVariantName();

}
