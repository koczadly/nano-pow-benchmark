package uk.oczadly.karl.nanopowbench.benchmark.opencl.kernel.executor;

import uk.oczadly.karl.nanopowbench.benchmark.exception.BenchmarkInitException;
import uk.oczadly.karl.nanopowbench.benchmark.opencl.CLDevice;
import uk.oczadly.karl.nanopowbench.benchmark.opencl.kernel.program.KernelProgramSource;

public interface KernelExecutorFactory {

    CLKernelExecutor create(KernelProgramSource program, CLDevice device, long globalWorkSize, long localWorkSize)
            throws BenchmarkInitException;

}
