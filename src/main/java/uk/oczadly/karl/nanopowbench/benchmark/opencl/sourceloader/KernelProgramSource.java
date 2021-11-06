package uk.oczadly.karl.nanopowbench.benchmark.opencl.sourceloader;

import uk.oczadly.karl.nanopowbench.benchmark.exception.BenchmarkInitException;

public interface KernelProgramSource {

    String load() throws BenchmarkInitException;

    String toDisplayString();

}
