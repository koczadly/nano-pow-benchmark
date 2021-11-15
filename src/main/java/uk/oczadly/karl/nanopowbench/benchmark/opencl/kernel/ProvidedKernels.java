package uk.oczadly.karl.nanopowbench.benchmark.opencl.kernel;

import uk.oczadly.karl.nanopowbench.benchmark.opencl.kernel.executor.KernelExecutorFactory;
import uk.oczadly.karl.nanopowbench.benchmark.opencl.kernel.executor.NanoWorkKernelExecutor;
import uk.oczadly.karl.nanopowbench.benchmark.opencl.kernel.program.ProvidedKernelProgramSource;

public class ProvidedKernels {

    public static final String DEFAULT = "NN2";


    public static KernelExecutorFactory getExecutor(String variant) {
        switch (variant.toUpperCase()) {
            case "NN1":
            case "NN2":
                return NanoWorkKernelExecutor::new;
            default:
                throw new IllegalArgumentException("Unrecognized kernel variant \"" + variant + "\".");
        }
    }

    public static ProvidedKernelProgramSource getProgram(String variant) {
        switch (variant.toUpperCase()) {
            case "NN1":
                return new ProvidedKernelProgramSource("nano-node Blake2b V1 (as of 46475fa)", "NN1");
            case "NN2":
                return new ProvidedKernelProgramSource("nano-node Blake2b V2 (as of ff424af)", "NN2");
            default:
                throw new IllegalArgumentException("Unrecognized kernel variant \"" + variant + "\".");
        }
    }

}
