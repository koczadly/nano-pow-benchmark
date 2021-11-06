package uk.oczadly.karl.nanopowbench;

import org.apache.commons.cli.*;
import uk.oczadly.karl.nanopowbench.benchmark.opencl.kernel.ProvidedKernels;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CommandArguments {

    private static final String OPT_CL_DEVICE = "device";
    private static final String OPT_DURATION = "duration";
    private static final String OPT_THREAD_COUNT = "threads";
    private static final String OPT_DIFFICULTY = "difficulty";
    private static final String OPT_KERNEL_FILE = "kernel-file";
//    private static final String OPT_KERNEL_INTERFACE = "kernel-interface";
    private static final String OPT_KERNEL = "kernel";


    private final Options options = generateOptions();
    private CommandLine cmdLine;


    public void parse(String[] args) throws ParseException {
        this.cmdLine = new DefaultParser().parse(options, args);
    }

    public void printError(ParseException ex) {
        System.err.println("Invalid argument: " + ex.getMessage());
        new HelpFormatter().printHelp("npowbench", options);
    }

    public Optional<Integer[]> getCLDevice() throws ParseException {
        return getOption(OPT_CL_DEVICE, v -> {
            String[] vals = v.split(":");
            if (vals.length != 2)
                throw new IllegalArgumentException("Must specify platform and device ID");
            return new Integer[] { Integer.parseInt(vals[0]), Integer.parseInt(vals[1]) };
        });
    }

    public Integer getDuration() throws ParseException {
        return getOption(OPT_DURATION, Integer::parseInt).orElse(10);
    }

    public Optional<Long> getThreadCount() throws ParseException {
        return getOption(OPT_THREAD_COUNT, Long::parseLong);
    }

    public Set<Difficulty> getDifficulties() throws ParseException {
        return new HashSet<>(getOptions(OPT_DIFFICULTY, Difficulty::parseHex));
    }

    public Optional<Path> getKernelFile() throws ParseException {
        return getOption(OPT_KERNEL_FILE, Paths::get);
    }

//    public Optional<Integer> getKernelExecutor() throws ParseException {
//        return getOption(OPT_KERNEL_INTERFACE, Integer::parseInt);
//    }

    public Optional<ProvidedKernels.ProvidedKernel> getProvidedKernel() throws ParseException {
        return getOption(OPT_KERNEL, v -> ProvidedKernels.get(Integer.parseInt(v)));
    }


    private <T> Optional<T> getOption(String option, ParseFunction<String, T> parser) throws ParseException {
        List<T> vals = getOptions(option, parser);
        return vals.isEmpty() ? Optional.empty() : Optional.ofNullable(vals.get(0));
    }

    private <T> List<T> getOptions(String option, ParseFunction<String, T> parser) throws ParseException {
        String[] values = cmdLine.getOptionValues(option);
        if (values != null) {
            List<T> vals = new ArrayList<>(values.length);
            try {
                for (String val : values) {
                    vals.add(parser.apply(val));
                }
            } catch (Exception e) {
                throw new ParseException(e.toString());
            }
            return vals;
        } else {
            return Collections.emptyList();
        }
    }

    private interface ParseFunction<T, R> {
        R apply(T input) throws Exception;
    }


    private Options generateOptions() {
        return new Options()
                .addOption(Option.builder("g").longOpt(OPT_CL_DEVICE)
                        .desc("Sets OpenCL platform and device to benchmark")
                        .hasArg().numberOfArgs(1).argName("platid:devid")
                        .build())
                .addOption(Option.builder("d").longOpt(OPT_DURATION)
                        .desc("Specifies the total benchmark duration (default = 10s)")
                        .hasArg().numberOfArgs(1).argName("seconds")
                        .build())
                .addOption(Option.builder("t").longOpt(OPT_THREAD_COUNT)
                        .desc("Batch/thread count")
                        .hasArg().numberOfArgs(1).argName("threads")
                        .build())
                .addOption(Option.builder("D").longOpt(OPT_DIFFICULTY)
                        .desc("Compares the result against the difficulty threshold")
                        .hasArg().argName("difficulty")
                        .build())
                .addOption(Option.builder().longOpt(OPT_KERNEL_FILE)
                        .desc("Specifies a custom OpenCL kernel file")
                        .hasArg().numberOfArgs(1).argName("path to file")
                        .build())
//                .addOption(Option.builder().longOpt(OPT_KERNEL_INTERFACE)
//                        .desc("Specifies the version of the work kernel interface")
//                        .hasArg().numberOfArgs(1).argName("version")
//                        .build())
                .addOption(Option.builder("kv").longOpt(OPT_KERNEL)
                        .desc("Specifies the OpenCL kernel program to use (1 or 2)")
                        .hasArg().numberOfArgs(1).argName("version")
                        .build());
    }

}
