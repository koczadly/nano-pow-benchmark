package uk.oczadly.karl.nanopowbench.benchmark.opencl;

import org.jocl.CLException;
import uk.oczadly.karl.nanopowbench.benchmark.BenchmarkResults;
import uk.oczadly.karl.nanopowbench.benchmark.Benchmarker;
import uk.oczadly.karl.nanopowbench.benchmark.exception.BenchmarkConfigException;
import uk.oczadly.karl.nanopowbench.benchmark.exception.BenchmarkException;
import uk.oczadly.karl.nanopowbench.benchmark.exception.BenchmarkInitException;
import uk.oczadly.karl.nanopowbench.benchmark.opencl.kernel.executor.KernelExecutor;
import uk.oczadly.karl.nanopowbench.benchmark.opencl.kernel.ProvidedKernels;
import uk.oczadly.karl.nanopowbench.benchmark.opencl.kernel.program.FileKernelProgramSource;
import uk.oczadly.karl.nanopowbench.benchmark.opencl.kernel.program.KernelProgramSource;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Karl Oczadly
 */
public class CLBenchmarker implements Benchmarker {

    private static final int DEFAULT_WORK_SIZE = 1024 * 1024;

    private final KernelExecutor kernel;

    private CLBenchmarker(KernelExecutor kernel) {
        this.kernel = kernel;
    }

    @Override
    public LinkedHashMap<String, String> getParameters() {
        CLDevice dev = kernel.getDevice();
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("Device",              dev.getDeviceName() + " (#" + dev.getID() + ")");
        params.put("Platform",            dev.getPlatformName() + " (#" + dev.getPlatformId() + ")");
        params.put("Work size (threads)", String.format("%,d", kernel.getWorkSize()));
        params.put("Kernel program",      kernel.getProgram().toDisplayString());
        params.put("Kernel interface",    kernel.getDisplayName());
        return params;
    }

    @Override
    public BenchmarkResults run(long duration, TimeUnit durationUnit) throws BenchmarkException {
        long durationNs = durationUnit.toNanos(duration);
        long startTime = System.nanoTime(), timeElapsed, totalWorkTime = 0, iterations = 0;

        // Perform batch executions until time exceeded
        do {
            long batchStartTime = System.nanoTime();
            try {
                kernel.computeBatch();
            } catch (CLException e) {
                throw new BenchmarkException("OpenCL error occurred during benchmark!", e);
            }
            totalWorkTime += System.nanoTime() - batchStartTime;
            iterations++;
        } while ((timeElapsed = System.nanoTime() - startTime) < durationNs);

        return new BenchmarkResults(
                timeElapsed, totalWorkTime,
                iterations * kernel.getWorkSize(),
                iterations);
    }


    public static final class Builder {
        private CLDevice device = new CLDevice(0, 0);
        private long workSize = DEFAULT_WORK_SIZE;
        private KernelProgramSource kernelProgram;
        private KernelExecutor kernelExecutor;

        public Builder() {
            useProvidedKernel(ProvidedKernels.getLatest());
        }


        public Builder useDevice(int platformId, int deviceId) {
            this.device = new CLDevice(platformId, deviceId);
            return this;
        }

        public Builder setWorkSize(long workSize) {
            this.workSize = workSize;
            return this;
        }

        public Builder useKernelFile(Path filePath) {
            this.kernelProgram = new FileKernelProgramSource(filePath);
            return this;
        }

        public Builder useKernelExecutor(KernelExecutor executor) {
            this.kernelExecutor = executor;
            return this;
        }

        public Builder useProvidedKernel(ProvidedKernels.ProvidedKernel kernel) {
            this.kernelProgram = kernel.getProgramSource();
            this.kernelExecutor = kernel.getExecutor();
            return this;
        }

        public CLBenchmarker build() throws BenchmarkInitException {
            if (kernelProgram == null || kernelExecutor == null)
                throw new BenchmarkConfigException("Unrecognized kernel!");

            kernelExecutor.init(kernelProgram, device, workSize);
            return new CLBenchmarker(kernelExecutor);
        }
    }
}
