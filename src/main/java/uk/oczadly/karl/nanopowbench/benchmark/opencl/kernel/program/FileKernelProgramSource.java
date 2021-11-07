package uk.oczadly.karl.nanopowbench.benchmark.opencl.kernel.program;

import uk.oczadly.karl.nanopowbench.benchmark.exception.BenchmarkConfigException;
import uk.oczadly.karl.nanopowbench.benchmark.exception.BenchmarkInitException;
import uk.oczadly.karl.nanopowbench.util.FileUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileKernelProgramSource implements KernelProgramSource {

    private final Path file;

    public FileKernelProgramSource(Path file) {
        this.file = file;
    }


    @Override
    public String load() throws BenchmarkInitException {
        if (!Files.isRegularFile(file)) {
            throw new BenchmarkConfigException("Requested kernel file could not be found.");
        }
        try {
            return FileUtil.readFileAsString(Files.newInputStream(file));
        } catch (IOException | SecurityException e) {
            throw new BenchmarkInitException("Unable to read provided kernel file!", e);
        }
    }

    @Override
    public String toDisplayString() {
        return "File (" + file.toString() + ")";
    }

}
