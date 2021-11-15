package uk.oczadly.karl.nanopowbench.benchmark.opencl.kernel.executor;

import org.jocl.*;
import uk.oczadly.karl.nanopowbench.benchmark.exception.BenchmarkConfigException;
import uk.oczadly.karl.nanopowbench.benchmark.exception.BenchmarkInitException;
import uk.oczadly.karl.nanopowbench.benchmark.opencl.CLDevice;
import uk.oczadly.karl.nanopowbench.benchmark.opencl.kernel.program.KernelProgramSource;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.jocl.CL.*;

public abstract class CLKernelExecutor {

    static {
        setExceptionsEnabled(true); // Configure JOCL
    }

    private final String functionName, displayName;
    private final CLDevice device;
    private final KernelProgramSource program;
    // OpenCL buffers
    private final long[] globalWorkSize, localWorkSize;
    // OpenCL objects
    private cl_kernel clKernel;
    private cl_command_queue clCmdQueue;
    private cl_context clContext;
    private cl_device_id clDevice;
    private cl_program clProgram;

    public CLKernelExecutor(String functionName, String displayName,
                            KernelProgramSource program, CLDevice device,
                            long globalWorkSize, long localWorkSize) throws BenchmarkInitException {
        this.functionName = functionName;
        this.displayName = displayName;
        this.program = program;
        this.device = device;
        this.globalWorkSize = new long[] { globalWorkSize };
        this.localWorkSize = localWorkSize < 0 ? null : new long[] { localWorkSize };
        initDevice();
    }


    public final String getKernelFunctionName() {
        return functionName;
    }

    public final String getDisplayName() {
        return displayName;
    }

    public final CLDevice getDevice() {
        return device;
    }

    public final KernelProgramSource getProgram() {
        return program;
    }

    public final long getGlobalWorkSize() {
        return globalWorkSize[0];
    }

    public final Optional<Long> getLocalWorkSize() {
        return localWorkSize != null ? Optional.of(localWorkSize[0]) : Optional.empty();
    }

    public final void init() throws BenchmarkInitException {
        if (clKernel == null) {
            initKernel();
        }
    }

    protected abstract void initKernelArgs(cl_kernel clKernel, cl_context clContext);

    public void computeBatch() throws CLException {
        clEnqueueNDRangeKernel(clCmdQueue, clKernel, 1, null, globalWorkSize, localWorkSize, 0, null, null);
        clFinish(clCmdQueue); // Wait for completion
    }

    public void cleanup() {
        // Cleanup OpenCL objects
        clReleaseContext(clContext);
        clReleaseDevice(clDevice);
        clReleaseCommandQueue(clCmdQueue);
        clReleaseKernel(clKernel);
        clReleaseProgram(clProgram);
    }


    private void initDevice() throws BenchmarkInitException {
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
            clDevice = clDeviceIdBuffer[device.getID()];

            // Fetch device information
            clGetPlatformInfo(clPlatform, CL_PLATFORM_NAME, strBuffer.length, Pointer.to(strBuffer), longBuffer);
            device.setPlatformName(new String(strBuffer, 0, (int)longBuffer[0] - 1, StandardCharsets.UTF_8));
            clGetDeviceInfo(clDevice, CL_DEVICE_NAME, strBuffer.length, Pointer.to(strBuffer), longBuffer);
            device.setDeviceName(new String(strBuffer, 0, (int)longBuffer[0] - 1, StandardCharsets.UTF_8));

            // Create device objects
            clContext = clCreateContext(clContextProperties, 1, clDeviceIdBuffer, null, null, null);
            cl_queue_properties clQueueProperties = new cl_queue_properties();
            clCmdQueue = clCreateCommandQueueWithProperties(clContext, clDevice, clQueueProperties, null);
        } catch (CLException e) {
            throw new BenchmarkInitException("OpenCL error occurred during initialization!", e);
        }
    }

    private void initKernel() throws BenchmarkInitException {
        try {
            // Create the program from the source code
            String[] programCode = { program.load() };
            clProgram = clCreateProgramWithSource(clContext, 1, programCode, null, null);
            try {
                clBuildProgram(clProgram, 1, new cl_device_id[] { clDevice }, null, null, null);
            } catch (CLException e) {
                throw new BenchmarkInitException("Invalid kernel code!", e);
            }

            // Create the kernel and memory buffers
            try {
                clKernel = clCreateKernel(clProgram, getKernelFunctionName(), null);
                initKernelArgs(clKernel, clContext);
            } catch (CLException e) {
                throw new BenchmarkInitException("Couldn't construct OpenCL kernel!", e);
            }
        } catch (CLException e) {
            throw new BenchmarkInitException("OpenCL error occurred during initialization!", e);
        }
    }


    @Override
    protected void finalize() throws Throwable {
        try {
            cleanup();
        } finally {
            super.finalize();
        }
    }
}
