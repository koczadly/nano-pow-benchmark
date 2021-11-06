package uk.oczadly.karl.nanopowbench;

import org.apache.commons.cli.*;
import uk.oczadly.karl.nanopowbench.benchmark.BenchmarkResults;
import uk.oczadly.karl.nanopowbench.benchmark.Benchmarker;
import uk.oczadly.karl.nanopowbench.benchmark.BenchmarkerFactory;
import uk.oczadly.karl.nanopowbench.benchmark.exception.BenchmarkConfigException;
import uk.oczadly.karl.nanopowbench.benchmark.exception.BenchmarkException;
import uk.oczadly.karl.nanopowbench.benchmark.exception.BenchmarkInitException;
import uk.oczadly.karl.nanopowbench.util.MetricPrefix;
import uk.oczadly.karl.nanopowbench.util.Util;

import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author Karl Oczadly
 */
public class Main {

    private static final Difficulty DIFF_RECEIVE = new Difficulty(0xfffffe0000000000L, "R");
    private static final Difficulty DIFF_SEND = new Difficulty(0xfffffff800000000L, "S");


    public static void main(String[] rawArgs) {
        ConsolePrinter out = new ConsolePrinter(System.out, 2, 73);
        out.printHeader("NANO PoW BENCHMARK (Blake2b)", "https://github.com/koczadly/nano-pow-benchmark/");
        out.blankLine();

        CommandArguments args = new CommandArguments();
        try {
            // Parse args
            args.parse(rawArgs);
            Set<Difficulty> difficulties = args.getDifficulties();
            if (difficulties.isEmpty()) {
                difficulties.add(DIFF_RECEIVE);
                difficulties.add(DIFF_SEND);
            }

            // Configure benchmark
            Benchmarker bench = BenchmarkerFactory.create(args);

            // Print benchmark params
            out.printTitle("Benchmark parameters");
            out.printParams(1, bench.getParameters());
            out.blankLine();

            // Run benchmark
            out.println(String.format("Running benchmark for %,d seconds...", args.getDuration()));
            out.blankLine();
            BenchmarkResults result = bench.run(args.getDuration(), TimeUnit.SECONDS);

            // Calculate results
            double solsPerSec = result.getTotalHashes() / (result.getWorkTime() / 1e9);
//            double secsPerIteration = result.getWorkTime() / (double) result.getIterations() / 1e9;
            LinkedHashMap<String, String> resultParams = new LinkedHashMap<>();
            resultParams.put("Time elapsed",    String.format("%.3f seconds", result.getTimeElapsed() / 1e9));
            resultParams.put("Working time",    String.format("%.6f seconds", result.getWorkTime() / 1e9));
            resultParams.put("Hash rate",       MetricPrefix.format(solsPerSec, "H/s", true));
            resultParams.put("Computed hashes", String.format("%,d", result.getTotalHashes()));
            LinkedHashMap<String, String> diffParams = new LinkedHashMap<>();
            difficulties.stream().sorted()
                    .forEach(diff -> {
                        double diffProbability = Util.ulongToDouble(-diff.asLong()) / 0x1p64;
                        double diffAvgSecsPerWork = 1d / (diffProbability * solsPerSec);
                        diffParams.put(diff.toString(),
                                String.format("%s (%,.4f work/s)",
                                        MetricPrefix.format(diffAvgSecsPerWork, "s/work", false),
                                        1d / diffAvgSecsPerWork));
                    });

            // Print results
            out.printHeader("BENCHMARK RESULTS");
            out.printParams(0, resultParams);
            out.printTitle("Performance metrics for difficulty thresholds");
            out.printParams(1, diffParams);
            out.printSeparator();
        } catch (BenchmarkConfigException e) {
            System.err.println("Error in benchmark configuration: " + e.getMessage());
        } catch (BenchmarkInitException e) {
            System.err.println("Failed to initialize benchmark: " + e.getMessage());
            e.printStackTrace();
        } catch (BenchmarkException e) {
            System.err.println("An error occurred!");
            e.printStackTrace();
        } catch (ParseException e) {
            args.printError(e);
        } catch (Exception e) {
            System.err.println("Uncaught exception occurred!");
            e.printStackTrace();
        }
    }

}
