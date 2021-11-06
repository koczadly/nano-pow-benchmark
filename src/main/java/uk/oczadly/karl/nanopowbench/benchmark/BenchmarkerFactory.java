package uk.oczadly.karl.nanopowbench.benchmark;

import org.apache.commons.cli.ParseException;
import uk.oczadly.karl.nanopowbench.CommandArguments;
import uk.oczadly.karl.nanopowbench.benchmark.exception.BenchmarkInitException;
import uk.oczadly.karl.nanopowbench.benchmark.opencl.OCLBenchmarker;

import java.nio.file.Paths;

public class BenchmarkerFactory {

    public static Benchmarker create(CommandArguments args) throws ParseException, BenchmarkInitException {
        // todo: for now we only support GPU
        return createOCL(args);
    }


    private static Benchmarker createOCL(CommandArguments args) throws ParseException, BenchmarkInitException {
        OCLBenchmarker.Builder builder = new OCLBenchmarker.Builder();
        args.getCLDevice().ifPresent(device -> builder.useDevice(device[0], device[1]));
        args.getThreadCount().ifPresent(builder::setWorkSize);
        args.getKernelFile().map(Paths::get).ifPresent(builder::useKernelFile);
        args.getKernelVersion().ifPresent(builder::useProvidedKernel);
        return builder.build();
    }

}
