package uk.oczadly.karl.nanopowbench.benchmark.opencl.kernel;

public class ProvidedKernels {

    private static final int LATEST = 2;


    public static ProvidedKernel getLatest() {
        return get(LATEST);
    }

    public static ProvidedKernel get(int version) {
        switch (version) {
            case 1:
                return new ProvidedKernel(
                        new ProvidedKernelProgramSource("nano-node V1 (as of commit 46475fa)", 1),
                        new NanoWorkKernelExecutor());
            case 2:
                return new ProvidedKernel(
                    new ProvidedKernelProgramSource("nano-node V2 (as of commit ff424af)", 2),
                    new NanoWorkKernelExecutor());
            default: throw new IllegalArgumentException("Unrecognized kernel version \"" + version + "\".");
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
