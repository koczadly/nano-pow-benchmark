package uk.oczadly.karl.nanopowbench.benchmark.opencl.sourceloader;

import uk.oczadly.karl.nanopowbench.benchmark.exception.BenchmarkConfigException;
import uk.oczadly.karl.nanopowbench.benchmark.exception.BenchmarkInitException;
import uk.oczadly.karl.nanopowbench.util.FileUtil;

import java.io.IOException;
import java.io.InputStream;

public class ProvidedKernelProgramSource implements KernelProgramSource {

    private final int version;

    public ProvidedKernelProgramSource() {
        this(2); // Default kernel version
    }

    public ProvidedKernelProgramSource(int version) {
        this.version = version;
    }


    @Override
    public String load() throws BenchmarkInitException {
        InputStream resource = ProvidedKernelProgramSource.class.getClassLoader()
                .getResourceAsStream("nano_work_v" + version + ".cl");
        if (resource == null) {
            throw new BenchmarkConfigException("Unrecognized or unsupported kernel version.");
        }
        try {
            return FileUtil.readFileAsString(resource);
        } catch (IOException e) {
            throw new BenchmarkInitException("Unable to read provided kernel file!", e);
        }
    }

    @Override
    public String toDisplayString() {
        return "nano-node, version " + version;
    }

}
