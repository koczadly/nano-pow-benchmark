package uk.oczadly.karl.nanopowbench.benchmark.opencl.kernel;

import uk.oczadly.karl.nanopowbench.benchmark.exception.BenchmarkInitException;

public interface KernelProgramSource {

    String load() throws BenchmarkInitException;

    String toDisplayString();

}
