package uk.oczadly.karl.nanopowbench.benchmark.opencl.kernel;

import org.jocl.*;
import uk.oczadly.karl.nanopowbench.benchmark.exception.BenchmarkConfigException;
import uk.oczadly.karl.nanopowbench.benchmark.exception.BenchmarkInitException;
import uk.oczadly.karl.nanopowbench.benchmark.opencl.CLDevice;
import uk.oczadly.karl.nanopowbench.benchmark.opencl.sourceloader.KernelProgramSource;

import java.nio.charset.StandardCharsets;

import static org.jocl.CL.*;

/**
 * Kernel executor for V1 "{@code nano_work}" kernels.
 */
public class NanoWorkKernelExecutor implements OCLKernelExecutor {

    private final String kernelFunctionName;
    private final CLDevice device;
    private final KernelProgramSource programSource;

    private final long[] workSize = new long[1];
    private cl_kernel clKernel;
    private cl_command_queue clCmdQueue;

    public NanoWorkKernelExecutor(String kernelFunctionName, CLDevice device, KernelProgramSource programSource) {
        this.kernelFunctionName = kernelFunctionName;
        this.device = device;
        this.programSource = programSource;
    }


    @Override
    public void configure(long workSize) throws BenchmarkInitException {
        if (workSize < 1)
            throw new BenchmarkConfigException("Invalid thread count/work size.");
        this.workSize[0] = workSize;
    }

    @Override
    public void computeIteration() throws CLException {
        clEnqueueNDRangeKernel(clCmdQueue, clKernel, 1, null, workSize, null, 0, null, null);
        clFinish(clCmdQueue); // Wait for completion
    }

    @Override
    public String getVariantName() {
        return "V1 'nano_work'";
    }

    @Override
    public void init() throws BenchmarkInitException {
        if(this.clKernel != null) return; // Already initialized
        try {
            // Configure JOCL
            setExceptionsEnabled(true);

            // Reusable buffer objects
            byte[] strBuffer = new byte[256];
            long[] longBuffer = new long[1];
            int[] intBuffer = new int[1];

            // Fetch device platform ID
            clGetPlatformIDs(0, null, intBuffer);
            int numPlatforms = intBuffer[0];
            if (device.getPlatformId() >= numPlatforms)
                throw new BenchmarkConfigException("Platform ID " + device.getPlatformId() + " not recognized.");
            cl_platform_id[] platforms = new cl_platform_id[numPlatforms];
            clGetPlatformIDs(platforms.length, platforms, null);
            cl_platform_id clPlatform = platforms[device.getPlatformId()];
            cl_context_properties clContextProperties = new cl_context_properties();
            clContextProperties.addProperty(CL_CONTEXT_PLATFORM, clPlatform);

            // Fetch device ID
            clGetDeviceIDs(clPlatform, CL_DEVICE_TYPE_ALL, 0, null, intBuffer);
            int numDevices = intBuffer[0];
            if (device.getID() >= numDevices)
                throw new BenchmarkConfigException("Device ID " + device.getID() + " not recognized.");
            cl_device_id[] clDeviceIdBuffer = new cl_device_id[numDevices];
            clGetDeviceIDs(clPlatform, CL_DEVICE_TYPE_ALL, numDevices, clDeviceIdBuffer, null);
            cl_device_id clDevice = clDeviceIdBuffer[device.getID()];

            // Fetch device information
            clGetPlatformInfo(clPlatform, CL_PLATFORM_NAME, strBuffer.length, Pointer.to(strBuffer), longBuffer);
            device.setPlatformName(new String(strBuffer, 0, (int)longBuffer[0] - 1, StandardCharsets.UTF_8));
            clGetDeviceInfo(clDevice, CL_DEVICE_NAME, strBuffer.length, Pointer.to(strBuffer), longBuffer);
            device.setDeviceName(new String(strBuffer, 0, (int)longBuffer[0] - 1, StandardCharsets.UTF_8));

            // Create device objects
            cl_context clContext = clCreateContext(clContextProperties, 1, clDeviceIdBuffer, null, null, null);
            cl_queue_properties clQueueProperties = new cl_queue_properties();
            clCmdQueue = clCreateCommandQueueWithProperties(clContext, clDevice, clQueueProperties, null);

            // Create the program from the source code
            String program = programSource.load();
            cl_program clProgram = clCreateProgramWithSource(clContext, 1, new String[] { program }, null, null);
            try {
                clBuildProgram(clProgram, 1, clDeviceIdBuffer, null, null, null);
            } catch (CLException e) {
                throw new BenchmarkInitException("Invalid kernel program file!", e);
            }

            // Create the kernel and memory buffers
            cl_mem clMemAttempt = clCreateBuffer(clContext,
                    CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                    Sizeof.cl_ulong, Pointer.to(new long[1]), null);
            cl_mem clMemResult = clCreateBuffer(clContext,
                    CL_MEM_WRITE_ONLY | CL_MEM_HOST_READ_ONLY,
                    Sizeof.cl_ulong, null, null);
            cl_mem clMemRoot = clCreateBuffer(clContext,
                    CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                    Sizeof.cl_uchar * 32, Pointer.to(new byte[32]), null);
            cl_mem clMemDifficulty = clCreateBuffer(clContext,
                    CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                    Sizeof.cl_ulong, Pointer.to(new long[] { -1L }), null);
            try {
                clKernel = clCreateKernel(clProgram, kernelFunctionName, null);
                clSetKernelArg(clKernel, 0, Sizeof.cl_mem, Pointer.to(clMemAttempt));
                clSetKernelArg(clKernel, 1, Sizeof.cl_mem, Pointer.to(clMemResult));
                clSetKernelArg(clKernel, 2, Sizeof.cl_mem, Pointer.to(clMemRoot));
                clSetKernelArg(clKernel, 3, Sizeof.cl_mem, Pointer.to(clMemDifficulty));
            } catch (CLException e) {
                throw new BenchmarkInitException("Invalid kernel program file!", e);
            }

            // Cleanup memory
            clReleaseDevice(clDevice);
            clReleaseProgram(clProgram);
            clReleaseContext(clContext);
        } catch (CLException e) {
            throw new BenchmarkInitException("OpenCL error occurred during initialization!", e);
        }
    }

}
