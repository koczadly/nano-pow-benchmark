package uk.oczadly.karl.nanopowbench.benchmark.opencl.kernel.program;

import uk.oczadly.karl.nanopowbench.benchmark.exception.BenchmarkInitException;

public interface KernelProgramSource {

    String load() throws BenchmarkInitException;

    String getDescriptiveName();

}
