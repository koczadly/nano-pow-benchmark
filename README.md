# Nano PoW benchmark
This is a tool used for benchmarking [Nano's](https://github.com/nanocurrency/nano-node) 
 [proof of work](https://docs.nano.org/integration-guides/work-generation/) generation on OpenCL devices.

Unlike other POW benchmarking tools, the hash rate of the Blake2b algorithm is measured directly, eliminating the
 random aspect of typical benchmarking methods. This leads to much faster benchmarking tests, with more accurate and 
 reliable results.

Note that this tool only supports OpenCL work generation using the standard `nano_work` kernel. Not all devices
 (including most CPUs) will support this capability. You may also need to install a separate OpenCL driver for your
 hardware devices.

## How to use
[Download the latest built jar](https://github.com/koczadly/nano-pow-benchmark/releases/latest/download/npowbench.jar), 
 or clone the repository and build the project from source using Maven.

This program requires Java 8 to be installed on your system. You can run the program from the command line like so:
```text
java -jar npowbench.jar --gpu 0:0 --duration 10
```

## Example output
```text
=====================================================
            NANO PoW BENCHMARK (Blake2b)
   https://github.com/koczadly/nano-pow-benchmark/
=====================================================

Using OpenCL device:
  - Platform: NVIDIA CUDA (0)
  - Device: GeForce GTX 980 Ti (0)
  - Max supported local work group size: 1,024

Benchmark parameters:
  - Thread count (global work size): 1,048,576
  - Local work size: 1,024
  - Generation kernel: [nano_node implementation]

Running benchmark for 10 seconds...

=====================================================
                  BENCHMARK RESULTS
=====================================================
Time elapsed: 10.000 seconds (9.992633s GPU work time)
Computation speed: 1.615 GH/s (16,139,681,792 total hashes)

Expected average generation times for difficulty thresholds:
  - fffffe0000000000: 5.194 ms/work (192.542 work/s)
  - fffffff800000000: 332.395 ms/work (3.008 work/s)
=====================================================
```