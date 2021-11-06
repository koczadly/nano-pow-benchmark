package uk.oczadly.karl.nanopowbench.benchmark.exception;

public class BenchmarkInitException extends BenchmarkException {

    public BenchmarkInitException() {
    }

    public BenchmarkInitException(String message) {
        super(message);
    }

    public BenchmarkInitException(String message, Throwable cause) {
        super(message, cause);
    }

    public BenchmarkInitException(Throwable cause) {
        super(cause);
    }

    public BenchmarkInitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
