package uk.oczadly.karl.nanopowbench.benchmark.opencl.kernel;

import org.jocl.*;

import static org.jocl.CL.*;

/**
 * Kernel executor for V1 "{@code nano_work}" kernels.
 */
public class NanoWorkKernelExecutor extends KernelExecutor {

    @Override
    public String getDisplayName() {
        return getKernelFunctionName() + "(attempt, result, item, diff)";
    }

    @Override
    public String getKernelFunctionName() {
        return "nano_work";
    }

    @Override
    protected void initKernel(cl_kernel clKernel, cl_context clContext) {
        // Create argument buffers
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
        // Assign arguments
        clSetKernelArg(clKernel, 0, Sizeof.cl_mem, Pointer.to(clMemAttempt));
        clSetKernelArg(clKernel, 1, Sizeof.cl_mem, Pointer.to(clMemResult));
        clSetKernelArg(clKernel, 2, Sizeof.cl_mem, Pointer.to(clMemRoot));
        clSetKernelArg(clKernel, 3, Sizeof.cl_mem, Pointer.to(clMemDifficulty));
    }
}
