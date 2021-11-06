# Nano PoW benchmark
This is a tool used for benchmarking [Nano's](https://github.com/nanocurrency/nano-node) 
 [proof of work](https://docs.nano.org/integration-guides/work-generation/) generation on OpenCL devices.

Unlike other POW benchmarking tools, the hash rate of the Blake2b algorithm is measured directly, eliminating the
 random aspect of typical benchmarking solutions. This leads to much quicker benchmarks, while providing more accurate
 and reliable results.

Tests over 10 seconds in duration should provide reasonable accuracy, providing there are no resource-intensive
 background apps running.

**This tool is only compatible with OpenCL-enabled hardware** such as graphics cards. You may also need to install an 
 OpenCL driver for your device if it isn't detected. For benchmarking incompatible devices, try using the facility 
 included with the [Nano Work Server](https://github.com/nanocurrency/nano-work-server). 

## How to use
[Download the latest built jar](https://github.com/koczadly/nano-pow-benchmark/releases/latest/download/npowbench.jar), 
 or clone the repository and build the project from source using Maven.

This program requires Java 8 (or above) to be installed on your system, and possibly an OpenCL driver.

You can run the program from the command line like so:

```text
java -jar npowbench.jar --device 0:0 --duration 10
```

### Command line options
Option | Description 
--- | --- 
`--device <plat:dev>` | Specify OpenCL device and platform IDs
`--duration <seconds>` | Benchmark duration in seconds
`--threads <threads>` | Thread count (global work size)
`--difficulty <diff>` | Output calculations for the specified difficulty threshold (as hexadecimal)<br>*Multiple difficulties may be provided*
`--kernel <version>` | Specify which *nano-node* kernel version to use<br>*Supported versions: **1**, **2***
`--kernel-file <path>` | Specify a custom OpenCL kernel program file<br>*The program must be compatible with `nano_work`*

## Example
### Command
```text
java -jar npowbench.jar --device 0:0 --duration 15 --difficulty fffffe0000000000
```

### Output
```text
=========================================================================
                      NANO PoW BENCHMARK (Blake2b)
             https://github.com/koczadly/nano-pow-benchmark/
=========================================================================

Benchmark parameters:
   Device:              Intel(R) HD Graphics 530 (#0)
   Platform:            Intel(R) OpenCL HD Graphics (#0)
   Work size (threads): 1,048,576
   Kernel program:      nano-node V2 (as of commit ff424af)
   Kernel interface:    nano_work(a, r, i, d)

Running benchmark for 15 seconds...

=========================================================================
                            BENCHMARK RESULTS
=========================================================================
Benchmark measurements:
   Total time elapsed:     15.009 s
   Total computation time: 15.008302 s
   Batch computation time: 33.278 ms
   Hash rate:              31.510 MH/s
   Total computed hashes:  472,907,776

Expected performance for difficulty thresholds:
   fffffe0000000000 [receive]: 266.223 ms/work (3.7563 work/s)
   fffffff800000000 [send]:    17.038 s/work (0.0587 work/s)
=========================================================================
```