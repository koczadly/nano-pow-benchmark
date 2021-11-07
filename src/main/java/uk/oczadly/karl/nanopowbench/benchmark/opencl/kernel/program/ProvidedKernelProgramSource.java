package uk.oczadly.karl.nanopowbench.benchmark.opencl.kernel.program;

import uk.oczadly.karl.nanopowbench.benchmark.exception.BenchmarkInitException;
import uk.oczadly.karl.nanopowbench.util.FileUtil;

import java.io.IOException;
import java.io.InputStream;

public class ProvidedKernelProgramSource implements KernelProgramSource {

    private final String displayName, resourceName;

    public ProvidedKernelProgramSource(String displayName, String shortName) {
        this.displayName = displayName;
        this.resourceName = "cl-kernel/" + shortName + ".cl";
    }


    @Override
    public String load() throws BenchmarkInitException {
        InputStream resource = ProvidedKernelProgramSource.class.getClassLoader().getResourceAsStream(resourceName);
        if (resource == null) {
            throw new BenchmarkInitException("Couldn't load kernel source file.");
        }
        try {
            return FileUtil.readFileAsString(resource);
        } catch (IOException e) {
            throw new BenchmarkInitException("Unable to read provided kernel file!", e);
        }
    }

    @Override
    public String toDisplayString() {
        return displayName;
    }

}
