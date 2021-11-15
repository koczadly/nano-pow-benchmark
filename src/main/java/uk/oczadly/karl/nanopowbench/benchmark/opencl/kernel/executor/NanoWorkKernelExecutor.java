package uk.oczadly.karl.nanopowbench.benchmark.opencl.kernel.executor;

import org.jocl.*;
import uk.oczadly.karl.nanopowbench.benchmark.exception.BenchmarkInitException;
import uk.oczadly.karl.nanopowbench.benchmark.opencl.CLDevice;
import uk.oczadly.karl.nanopowbench.benchmark.opencl.kernel.program.KernelProgramSource;

import static org.jocl.CL.*;
import static org.jocl.CL.clSetKernelArg;

/**
 * Kernel executor for V1 "{@code nano_work}" kernels.
 */
public class NanoWorkKernelExecutor extends CLKernelExecutor {

    private cl_mem clMemAttempt, clMemResult, clMemRoot, clMemDifficulty;

    public NanoWorkKernelExecutor(KernelProgramSource program, CLDevice device, long globalWorkSize, long localWorkSize)
            throws BenchmarkInitException {
        super("nano_work", "nano_work(attempt, result, item, diff)",
                program, device, globalWorkSize, localWorkSize);
    }


    @Override
    protected void initKernelArgs(cl_kernel clKernel, cl_context clContext) {
        // Create argument buffers
        clMemAttempt = clCreateBuffer(clContext,
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_ulong, Pointer.to(new long[1]), null);
        clMemResult = clCreateBuffer(clContext,
                CL_MEM_WRITE_ONLY | CL_MEM_HOST_READ_ONLY,
                Sizeof.cl_ulong, null, null);
        clMemRoot = clCreateBuffer(clContext,
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_uchar * 32, Pointer.to(new byte[32]), null);
        clMemDifficulty = clCreateBuffer(clContext,
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_ulong, Pointer.to(new long[] { -1L }), null);

        // Assign arguments
        clSetKernelArg(clKernel, 0, Sizeof.cl_mem, Pointer.to(clMemAttempt));
        clSetKernelArg(clKernel, 1, Sizeof.cl_mem, Pointer.to(clMemResult));
        clSetKernelArg(clKernel, 2, Sizeof.cl_mem, Pointer.to(clMemRoot));
        clSetKernelArg(clKernel, 3, Sizeof.cl_mem, Pointer.to(clMemDifficulty));
    }

    @Override
    public void cleanup() {
        try {
            if (clMemAttempt != null) {
                clReleaseMemObject(clMemAttempt);
                clReleaseMemObject(clMemResult);
                clReleaseMemObject(clMemRoot);
                clReleaseMemObject(clMemDifficulty);
            }
        } finally {
            super.cleanup();
        }
    }
}
