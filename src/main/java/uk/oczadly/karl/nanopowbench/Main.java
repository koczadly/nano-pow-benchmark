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

    private static final Difficulty DIFF_RECEIVE = new Difficulty(Difficulty.DIFF_V2_RECEIVE);
    private static final Difficulty DIFF_SEND = new Difficulty(Difficulty.DIFF_V2_SEND);


    public static void main(String[] rawArgs) {
        ConsolePrinter out = new ConsolePrinter(System.out, 3, 73);
        out.printHeader("NANO PoW BENCHMARK (Blake2b)", "https://github.com/koczadly/nano-pow-benchmark/");
        out.blankLine();

        CommandArguments args = new CommandArguments();
        try {
            // Parse args
            args.parse(rawArgs);
            Set<Difficulty> difficulties = args.getDifficulties();
            difficulties.add(DIFF_RECEIVE);
            difficulties.add(DIFF_SEND);

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
            double hashrate = result.getTotalHashes() / (result.getWorkTime() / 1e9);
            double secsPerIteration = (result.getWorkTime() / 1e9) / result.getIterations();

            LinkedHashMap<String, String> resultParams = new LinkedHashMap<>();
            resultParams.put("Total time elapsed", String.format("%.3f s", result.getTimeElapsed() / 1e9));
            resultParams.put("Total computation time", String.format("%.6f s", result.getWorkTime() / 1e9));
            resultParams.put("Batch computation time", MetricPrefix.format(secsPerIteration, "s", false));
            resultParams.put("Hash rate", MetricPrefix.format(hashrate, "H/s", true));
            resultParams.put("Total computed hashes", String.format("%,d", result.getTotalHashes()));

            LinkedHashMap<String, String> diffParams = new LinkedHashMap<>();
            difficulties.stream().sorted()
                    .forEach(diff -> {
                        double diffProbability = Util.ulongToDouble(-diff.asLong()) / 0x1p64;
                        double diffAvgSecsPerWork = Math.max(1d / (diffProbability * hashrate), secsPerIteration);
                        diffParams.put(diff.toString(),
                                String.format("%s (%,.4f work/s)",
                                        MetricPrefix.format(diffAvgSecsPerWork, "s/work", false),
                                        1d / diffAvgSecsPerWork));
                    });

            // Print results
            out.printHeader("BENCHMARK RESULTS");
            out.printTitle("Benchmark measurements");
            out.printParams(1, resultParams);
            out.blankLine();
            out.printTitle("Expected performance for difficulty thresholds");
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
