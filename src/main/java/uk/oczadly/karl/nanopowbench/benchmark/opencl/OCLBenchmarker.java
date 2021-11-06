package uk.oczadly.karl.nanopowbench.benchmark.opencl;

import org.jocl.CLException;
import uk.oczadly.karl.nanopowbench.benchmark.BenchmarkResults;
import uk.oczadly.karl.nanopowbench.benchmark.Benchmarker;
import uk.oczadly.karl.nanopowbench.benchmark.exception.BenchmarkException;
import uk.oczadly.karl.nanopowbench.benchmark.exception.BenchmarkInitException;
import uk.oczadly.karl.nanopowbench.benchmark.opencl.kernel.NanoWorkKernelExecutor;
import uk.oczadly.karl.nanopowbench.benchmark.opencl.kernel.OCLKernelExecutor;
import uk.oczadly.karl.nanopowbench.benchmark.opencl.sourceloader.FileKernelProgramSource;
import uk.oczadly.karl.nanopowbench.benchmark.opencl.sourceloader.KernelProgramSource;
import uk.oczadly.karl.nanopowbench.benchmark.opencl.sourceloader.ProvidedKernelProgramSource;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Karl Oczadly
 */
public class OCLBenchmarker implements Benchmarker {

    private static final int DEFAULT_THREAD_COUNT = 1024 * 1024;

    private final CLDevice device;
    private final long workSize;
    private final KernelProgramSource kernelSource;
    private final OCLKernelExecutor kernelExecutor;

    private OCLBenchmarker(CLDevice device, long workSize, KernelProgramSource kernelSource)
            throws BenchmarkInitException {
        this.device = device;
        this.workSize = workSize;
        this.kernelSource = kernelSource;
        this.kernelExecutor = new NanoWorkKernelExecutor("nano_work", device, kernelSource);
        kernelExecutor.init();
        kernelExecutor.configure(workSize);
    }

    @Override
    public LinkedHashMap<String, String> getParameters() {
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("Device",              device.getDeviceName() + " (#" + device.getID() + ")");
        params.put("Platform",            device.getPlatformName() + " (#" + device.getPlatformId() + ")");
        params.put("Work size (threads)", String.format("%,d", workSize));
        params.put("Kernel program",      kernelSource.toDisplayString());
        params.put("Kernel executor",     kernelExecutor.getVariantName());
        return params;
    }

    @Override
    public BenchmarkResults run(long duration, TimeUnit durationUnit) throws BenchmarkException {
        long durationNs = durationUnit.toNanos(duration);
        long startTime = System.nanoTime(), timeElapsed, totalWorkTime = 0, iterations = 0;
        do {
            // Perform a batch iteration of work generation
            long batchStartTime = System.nanoTime();
            try {
                kernelExecutor.computeIteration();
            } catch (CLException e) {
                throw new BenchmarkException("OpenCL error occurred during benchmark!", e);
            }
            totalWorkTime += System.nanoTime() - batchStartTime;
            iterations++;
        } while ((timeElapsed = System.nanoTime() - startTime) < durationNs);
        return new BenchmarkResults(timeElapsed, totalWorkTime, iterations * workSize, iterations);
    }


    public static final class Builder {
        private CLDevice device = new CLDevice(0, 0);
        private long threadCount = DEFAULT_THREAD_COUNT;
        private KernelProgramSource kernel = new ProvidedKernelProgramSource();

        public Builder useDevice(int platformId, int deviceId) {
            this.device = new CLDevice(platformId, deviceId);
            return this;
        }

        public Builder setWorkSize(long threadCount) {
            this.threadCount = threadCount;
            return this;
        }

        public Builder useKernelFile(Path filePath) {
            this.kernel = new FileKernelProgramSource(filePath);
            return this;
        }

        public Builder useProvidedKernel(int version) {
            this.kernel = new ProvidedKernelProgramSource(version);
            return this;
        }

        public OCLBenchmarker build() throws BenchmarkInitException {
            return new OCLBenchmarker(device, threadCount, kernel);
        }
    }
}
