package uk.oczadly.karl.nanopowbench.benchmark.opencl.kernel.executor;

import org.jocl.*;
import uk.oczadly.karl.nanopowbench.benchmark.exception.BenchmarkConfigException;
import uk.oczadly.karl.nanopowbench.benchmark.exception.BenchmarkInitException;
import uk.oczadly.karl.nanopowbench.benchmark.opencl.CLDevice;
import uk.oczadly.karl.nanopowbench.benchmark.opencl.kernel.program.KernelProgramSource;

import java.nio.charset.StandardCharsets;

import static org.jocl.CL.*;

public abstract class KernelExecutor {

    // Config
    private CLDevice device;
    private KernelProgramSource program;
    // OpenCL variables
    private final long[] workSize = new long[1];
    private cl_kernel clKernel;
    private cl_command_queue clCmdQueue;


    public CLDevice getDevice() {
        return device;
    }

    public KernelProgramSource getProgram() {
        return program;
    }

    public long getWorkSize() {
        return workSize[0];
    }


    public abstract String getDisplayName();

    public abstract String getKernelFunctionName();

    protected abstract void initKernel(cl_kernel clKernel, cl_context clContext);

    public void computeBatch() throws CLException {
        clEnqueueNDRangeKernel(clCmdQueue, clKernel, 1, null, workSize, null, 0, null, null);
        clFinish(clCmdQueue); // Wait for completion
    }


    public final void init(KernelProgramSource program, CLDevice device, long workSize) throws BenchmarkInitException {
        this.workSize[0] = workSize;

        if (this.program != null) return; // Already initialized

        this.program = program;
        this.device = device;
        setExceptionsEnabled(true); // Configure JOCL
        try {
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
            String[] programCode = { program.load() };
            cl_program clProgram = clCreateProgramWithSource(clContext, 1, programCode, null, null);
            try {
                clBuildProgram(clProgram, 1, clDeviceIdBuffer, null, null, null);
            } catch (CLException e) {
                throw new BenchmarkInitException("Invalid kernel code!", e);
            }

            // Create the kernel and memory buffers
            try {
                clKernel = clCreateKernel(clProgram, getKernelFunctionName(), null);
                initKernel(clKernel, clContext);
            } catch (CLException e) {
                throw new BenchmarkInitException("Couldn't construct OpenCL kernel!", e);
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
