package uk.oczadly.karl.nanopowbench;

import org.jocl.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.jocl.CL.*;

/**
 * @author Karl Oczadly
 */
public class Benchmarker {
    
    private volatile cl_command_queue clQueue;
    private volatile cl_kernel clKernel;
    
    public Device initCL(int platformId, int deviceId, Path kernelPath) throws Exception {
        String programSrc = kernelPath != null
                ? readProgramSource(Files.newInputStream(kernelPath))
                : readProgramSource();
    
        try {
            setExceptionsEnabled(true);
    
            // Obtain the number of platforms
            int[] numPlatformsArray = new int[1];
            clGetPlatformIDs(0, null, numPlatformsArray);
            int numPlatforms = numPlatformsArray[0];
            if (platformId >= numPlatforms)
                throw new CLException("Platform ID not recognized.");
    
            // Obtain a platform ID
            cl_platform_id[] platforms = new cl_platform_id[numPlatforms];
            clGetPlatformIDs(platforms.length, platforms, null);
            cl_platform_id platform = platforms[platformId];
    
            // Initialize the context properties
            cl_context_properties contextProperties = new cl_context_properties();
            contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
    
            // Obtain the number of devices for the platform
            int[] numDevicesArray = new int[1];
            clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, 0, null, numDevicesArray);
            int numDevices = numDevicesArray[0];
            if (deviceId >= numDevices)
                throw new CLException("Device ID not recognized.");
    
            // Obtain a device ID
            cl_device_id[] devices = new cl_device_id[numDevices];
            clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, numDevices, devices, null);
            cl_device_id device = devices[deviceId];
    
            // Fetch device and platform info
            byte[] strBuffer = new byte[256];
            long[] strBufferLen = new long[1];
            
            clGetPlatformInfo(platform, CL_PLATFORM_NAME, strBuffer.length, Pointer.to(strBuffer), strBufferLen);
            String platformName = new String(strBuffer, 0, (int)strBufferLen[0] - 1, StandardCharsets.UTF_8);
            
            clGetDeviceInfo(device, CL_DEVICE_NAME, strBuffer.length, Pointer.to(strBuffer), strBufferLen);
            String deviceName = new String(strBuffer, 0, (int)strBufferLen[0] - 1, StandardCharsets.UTF_8);
    
    
            long[] workGroupSizeArray = new long[1];
            clGetDeviceInfo(device, CL_DEVICE_MAX_WORK_GROUP_SIZE, Sizeof.cl_ulong,
                    Pointer.to(workGroupSizeArray), null);
            long workGroupSize = workGroupSizeArray[0];
    
            // Create a context for the selected device
            cl_context context = clCreateContext(
                    contextProperties, 1, new cl_device_id[] { device }, null, null, null);
    
            // Create a command-queue for the selected device
            cl_queue_properties properties = new cl_queue_properties();
            clQueue = clCreateCommandQueueWithProperties(context, device, properties, null);
    
            // Create the program from the source code
            cl_program program = clCreateProgramWithSource(
                    context, 1, new String[] { programSrc }, null, null);
            clBuildProgram(program, 1, new cl_device_id[] { device }, null, null, null);
    
            // Buffers and set default values
            cl_mem clMemAttempt = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                    Sizeof.cl_ulong, Pointer.to(new long[1]), null);
            cl_mem clMemResult = clCreateBuffer(context, CL_MEM_WRITE_ONLY | CL_MEM_HOST_READ_ONLY,
                    Sizeof.cl_ulong, null, null);
            cl_mem clMemRoot = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                    Sizeof.cl_uchar * 32, Pointer.to(new byte[32]), null);
            cl_mem clMemDifficulty = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                    Sizeof.cl_ulong, Pointer.to(new long[] { -1L }), null);
    
            // Create the kernel
            clKernel = clCreateKernel(program, "nano_work", null);
            clSetKernelArg(clKernel, 0, Sizeof.cl_mem, Pointer.to(clMemAttempt));
            clSetKernelArg(clKernel, 1, Sizeof.cl_mem, Pointer.to(clMemResult));
            clSetKernelArg(clKernel, 2, Sizeof.cl_mem, Pointer.to(clMemRoot));
            clSetKernelArg(clKernel, 3, Sizeof.cl_mem, Pointer.to(clMemDifficulty));
            
            // Cleanup memory
            clReleaseDevice(device);
            clReleaseProgram(program);
            clReleaseContext(context);
            
            return new Device(platformId, deviceId, workGroupSize, platformName, deviceName);
        } catch (org.jocl.CLException e) {
            throw new CLException("Couldn't initialize OpenCL parameters.", e);
        }
    }
    
    public BenchmarkResults benchmark(long duration, long threads, long localThreads) {
        long[] globalWorkSize = { threads };
        long[] localWorkSize = localThreads == -1 ? null : new long[] { localThreads };
    
        long durationNanos = duration * 1_000_000_000;
        long startTime = System.nanoTime(), timeElapsed, totalWorkTime = 0, iterations = 0;
        try {
            do {
                // Run a batch of work generation
                long batchStartTime = System.nanoTime();
                clEnqueueNDRangeKernel(clQueue, clKernel, 1, null, globalWorkSize, localWorkSize, 0, null, null);
                clFinish(clQueue); // Wait for completion
                totalWorkTime += System.nanoTime() - batchStartTime;
                iterations++;
            } while ((timeElapsed = System.nanoTime() - startTime) < durationNanos);
        } catch (org.jocl.CLException e) {
            throw new CLException("OpenCL error occurred during benchmark: " + e.getMessage(), e);
        }
        return new BenchmarkResults(timeElapsed, totalWorkTime, iterations * threads);
    }
    
    
    private static String readProgramSource() throws IOException {
        InputStream resource = Benchmarker.class.getClassLoader().getResourceAsStream("nano_work.cl");
        if (resource == null) throw new FileNotFoundException("Couldn't locate internal resource nano_work.cl");
        return readProgramSource(resource);
    }
    
    private static String readProgramSource(InputStream is) throws IOException {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null)
                sb.append(line).append('\n');
            return sb.toString();
        } finally {
            try {
                is.close();
            } catch (IOException ignored) {}
        }
    }
    
    private static String getString(cl_platform_id platform, int paramName) {
        // Obtain the length of the string that will be queried
        long size[] = new long[1];
        clGetPlatformInfo(platform, paramName, 0, null, size);
        
        // Create a buffer of the appropriate size and fill it with the info
        byte buffer[] = new byte[(int)size[0]];
        clGetPlatformInfo(platform, paramName, buffer.length, Pointer.to(buffer), null);
        
        // Create a string from the buffer (excluding the trailing \0 byte)
        return new String(buffer, 0, buffer.length-1);
    }
    
}
