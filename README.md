# Nano PoW benchmark
This is a tool used for benchmarking [Nano's](https://github.com/nanocurrency/nano-node) 
 [proof of work](https://docs.nano.org/integration-guides/work-generation/) generation on OpenCL devices.

Unlike other POW benchmarking tools, the hash rate of the Blake2b algorithm is measured directly, eliminating the
 random aspect of typical benchmarking methods. This leads to much faster benchmarking tests, with more accurate and 
 reliable results.

## How to use
Download the latest version from the [releases page](https://github.com/koczadly/nano-pow-benchmark/releases), or
 build the project from source.

This program requires Java 8 to be installed on your system. You can run the program from the console like so:
```text
java -jar npowbench.jar --gpu 0:0 --duration 30
```

## Example output
```text
===================================================
           NANO PoW BENCHMARK (Blake2b)
===================================================

Using OpenCL device:
  - Platform: NVIDIA CUDA                                                                                                                                                                                                                                                      (0)
  - Device: GeForce GTX 980 Ti                                                                                                                                                                                                                                               (0)
  - Max supported local work group size: 1,024

Benchmark parameters:
  - Thread count (global work size): 1,048,576
  - Local work size: [OpenCL default]
  - Generation kernel: [nano_node standard]

Running benchmark for 30 seconds...

===================================================
                 BENCHMARK RESULTS
===================================================
Time elapsed: 30.000 seconds (29.983404s GPU work time)
Computation speed: 1.660 GH/s (49,777,999,872 total hashes)

Expected average generation times for difficulty thresholds:
  - fffffe0000000000: 5.053 ms/work (197.909 work/s)
  - fffffff800000000: 323.380 ms/work (3.092 work/s)
===================================================
```