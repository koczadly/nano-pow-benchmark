package uk.oczadly.karl.nanopowbench;

import org.apache.commons.cli.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Karl Oczadly
 */
public class Main {
    
    public static void main(String[] args) {
        System.out.println("=====================================================");
        System.out.println("            NANO PoW BENCHMARK (Blake2b)");
        System.out.println("   https://github.com/koczadly/nano-pow-benchmark/");
        System.out.printf("=====================================================%n%n");
        
        Options options = new Options();
        options.addOption(Option.builder("g").longOpt("gpu")
                .desc("Sets OpenCL platform and device to benchmark")
                .hasArg().numberOfArgs(1).argName("platform:device")
                .required().build());
        options.addOption(Option.builder("d").longOpt("duration")
                .desc("Specifies the total benchmark duration (default = 20s)")
                .hasArg().numberOfArgs(1).argName("seconds")
                .build());
        options.addOption(Option.builder("t").longOpt("threads")
                .desc("Thread count (global work size, default = 1048576)")
                .hasArg().numberOfArgs(1).argName("threads")
                .build());
        options.addOption(Option.builder("s").longOpt("local-work-size")
                .desc("Specifies the local work size")
                .hasArg().numberOfArgs(1).argName("size")
                .build());
        options.addOption(Option.builder("D").longOpt("difficulty")
                .desc("Compares the result against the difficulty threshold")
                .hasArg().argName("difficulty")
                .build());
        options.addOption(Option.builder("k").longOpt("kernel")
                .desc("Specifies the OpenCL kernel file (must be compatible with default nano_work)")
                .hasArg().numberOfArgs(1).argName("file path")
                .build());
    
        try {
            CommandLineParser cmdLineParser = new DefaultParser();
            CommandLine cmdLine = cmdLineParser.parse(options, args);
            
            // Parse args
            String[] deviceSplit = cmdLine.getOptionValue("g", "0:0").split(":");
            int clPlat = Integer.parseInt(deviceSplit[0]);
            int clDevice = Integer.parseInt(deviceSplit[1]);
            int duration = Integer.parseInt(cmdLine.getOptionValue("d", "20"));
            long threads = Long.parseLong(cmdLine.getOptionValue("t", "1048576"));
            long localWorkSize = Long.parseLong(cmdLine.getOptionValue("s", "-1"));
            Path kernelFile = cmdLine.hasOption("k")
                    ? Paths.get(cmdLine.getOptionValue("k")).toAbsolutePath() : null;
            // Parse difficulties
            Set<Long> difficulties;
            if (cmdLine.hasOption("D")) {
                difficulties = Arrays.stream(cmdLine.getOptionValues("D"))
                        .map(s -> Long.parseUnsignedLong(s, 16))
                        .collect(Collectors.toSet());
            } else {
                difficulties = new HashSet<>();
                difficulties.add(0xfffffe0000000000L); // Receive thresh
                difficulties.add(0xfffffff800000000L); // Send thresh
            }
            
            Benchmarker benchmark = new Benchmarker();
            
            // Initialize devices
            Device device = benchmark.initCL(clPlat, clDevice, kernelFile);
            if (localWorkSize <= 0) {
                localWorkSize = -1;
            } else {
                if (localWorkSize > device.getMaxLocalWorkSize()) {
                    System.err.println("Local work size is too high (max: " + device.getMaxLocalWorkSize() + ").");
                    System.exit(1);
                    return;
                }
            }
            
            // Print device info
            System.out.println("Using OpenCL device:");
            System.out.printf("  - Platform: %s (%d)%n", device.getPlatformName(), device.getPlatformId());
            System.out.printf("  - Device: %s (%d)%n", device.getDeviceName(), device.getDeviceId());
            System.out.printf("  - Max supported local work group size: %,d%n%n", device.getMaxLocalWorkSize());
            
            // Benchmark
            System.out.println("Benchmark parameters:");
            System.out.printf("  - Thread count (global work size): %,d%n", threads);
            if (localWorkSize == -1) {
                System.out.printf("  - Local work size: [OpenCL default]%n");
            } else {
                System.out.printf("  - Local work size: %,d%n", localWorkSize);
            }
            System.out.printf("  - Generation kernel: %s%n%n",
                    kernelFile != null ? kernelFile : "[nano_node implementation]");
            
            System.out.printf("Running benchmark for %,d seconds...%n", duration);
            BenchmarkResults results = benchmark.benchmark(duration, threads, localWorkSize);
            double solsPerSec = results.getSolutions() / (results.getWorkTime() / 1e9);
            
            // Print results
            System.out.printf("%n=====================================================%n");
            System.out.println("                  BENCHMARK RESULTS");
            System.out.println("=====================================================");
            System.out.printf("Time elapsed: %.3f seconds (%.6fs GPU work time)%n",
                    results.getTotalTimeElapsed() / 1e9, results.getWorkTime() / 1e9);
            System.out.printf("Computation speed: %s (%,d total hashes)%n%n",
                    MetricPrefix.format(solsPerSec, "H/s", true), results.getSolutions());
            System.out.println("Expected average generation times for difficulty thresholds:");
            difficulties.stream().sorted(Long::compareUnsigned).forEach(diff -> {
                double diffSolsPerSec = (Util.ulongToDouble(-diff) / 0x1p64) * solsPerSec;
                System.out.printf("  - %s: %s/work (%,.3f work/s)%n",
                        Util.leftPadString(Long.toHexString(diff), 16, '0'),
                        MetricPrefix.format(1d / diffSolsPerSec, "s", false), diffSolsPerSec);
            });
            System.out.println("=====================================================");
        } catch (Exception e) {
            System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage());
            new HelpFormatter().printHelp("npowbench", options);
        }
    }

}
