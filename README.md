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
java -jar npowbench.jar [options...]
```

### Command line options
Option | Description 
--- | --- 
`--device <platform:device> | Specify OpenCL device ID and platform ID
`--duration <seconds>` | Benchmark duration in seconds
`--threads <threads>` | Thread count (global work size)
`--local-work-size <size>` | Set the OpenCL local work size
`--difficulty <diff>` | Output calculations for the specified difficulty threshold (as hexadecimal)<br>*Multiple difficulties may be provided*
`--kernel <variant>` | Specify which *nano-node* kernel variant to use<br>*Supported versions: **NN1**, **NN2***
`--kernel-file <path>` | Specify a custom OpenCL kernel program file<br>*The program must be compatible with `nano_work`*

## Example
### Command
```text
java -jar npowbench.jar --device 0:0 --duration 15 --difficulty fffffe0000000000
```

### Output
```text
=========================================================================
                      NANO PROOF-OF-WORK BENCHMARK
             https://github.com/koczadly/nano-pow-benchmark/
=========================================================================

Benchmark parameters:
   Device:                     Intel(R) HD Graphics 530 (#0)
   Platform:                   Intel(R) OpenCL HD Graphics (#0)
   Threads (global work size): 1,048,576
   Local work size:            Default
   Kernel program:             nano-node Blake2b V2 (as of ff424af)
   Kernel function:            nano_work(attempt, result, item, diff)

Running benchmark for 15 seconds... Please wait...

=========================================================================
                            BENCHMARK RESULTS
=========================================================================
Benchmark measurements:
   Total time elapsed:     15.009 s
   Total computation time: 15.008771 s
   Batch computation time: 33.502 ms
   Hash rate:              31.299 MH/s
   Total computed hashes:  469,762,048

Expected performance for difficulty thresholds:
   fffffe0000000000 [receive]: 3.7312 work/s (268.014 ms/work)
   fffffff800000000 [send]:    0.0583 work/s (17.153 s/work)
=========================================================================
```