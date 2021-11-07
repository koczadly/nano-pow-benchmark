package uk.oczadly.karl.nanopowbench.benchmark.opencl.kernel;

import uk.oczadly.karl.nanopowbench.benchmark.opencl.kernel.executor.KernelExecutor;
import uk.oczadly.karl.nanopowbench.benchmark.opencl.kernel.executor.NanoWorkKernelExecutor;
import uk.oczadly.karl.nanopowbench.benchmark.opencl.kernel.program.ProvidedKernelProgramSource;

public class ProvidedKernels {

    private static final String DEFAULT = "NN2";


    public static ProvidedKernel getDefault() {
        return get(DEFAULT);
    }

    public static ProvidedKernel get(String variant) {
        switch (variant.toUpperCase()) {
            case "NN1":
                return new ProvidedKernel(
                        new ProvidedKernelProgramSource("nano-node Blake2b V1 (as of 46475fa)", "NN1"),
                        new NanoWorkKernelExecutor());
            case "NN2":
                return new ProvidedKernel(
                    new ProvidedKernelProgramSource("nano-node Blake2b V2 (as of ff424af)", "NN2"),
                    new NanoWorkKernelExecutor());
            default:
                throw new IllegalArgumentException("Unrecognized kernel variant \"" + variant + "\".");
        }
    }


    public static class ProvidedKernel {
        private final ProvidedKernelProgramSource programSource;
        private final KernelExecutor executor;

        public ProvidedKernel(ProvidedKernelProgramSource programSource, KernelExecutor executor) {
            this.programSource = programSource;
            this.executor = executor;
        }

        public ProvidedKernelProgramSource getProgramSource() {
            return programSource;
        }

        public KernelExecutor getExecutor() {
            return executor;
        }
    }

}
