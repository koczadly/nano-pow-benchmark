package uk.oczadly.karl.nanopowbench.benchmark;

import org.apache.commons.cli.ParseException;
import uk.oczadly.karl.nanopowbench.CommandArguments;
import uk.oczadly.karl.nanopowbench.benchmark.exception.BenchmarkInitException;
import uk.oczadly.karl.nanopowbench.benchmark.opencl.CLBenchmarker;

public class BenchmarkerFactory {

    public static Benchmarker create(CommandArguments args) throws ParseException, BenchmarkInitException {
        // todo: for now we only support GPU
        return createOCL(args);
    }


    private static Benchmarker createOCL(CommandArguments args) throws ParseException, BenchmarkInitException {
        CLBenchmarker.Builder builder = new CLBenchmarker.Builder();
        args.getCLDevice().ifPresent(device -> builder.useDevice(device[0], device[1]));
        args.getThreadCount().ifPresent(builder::setGlobalWorkSize);
        args.getLocalWorkSize().ifPresent(builder::setLocalWorkSize);
        args.getProvidedKernel().ifPresent(builder::useProvidedKernel);
        args.getKernelFile().ifPresent(builder::useKernelFile);
//        args.getKernelExecutor().ifPresent(builder::useKernelExecutor);
        return builder.build();
    }

}
