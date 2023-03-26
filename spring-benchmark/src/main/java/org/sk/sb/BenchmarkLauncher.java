package org.sk.sb;

import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public final class BenchmarkLauncher {

    private BenchmarkLauncher() {
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ApplicationBenchmark.class.getSimpleName())
                .measurementIterations(1)
                .forks(200)
                .shouldFailOnError(true)
                .addProfiler(GCProfiler.class)
                .build();
        new Runner(opt).run();
    }
}
