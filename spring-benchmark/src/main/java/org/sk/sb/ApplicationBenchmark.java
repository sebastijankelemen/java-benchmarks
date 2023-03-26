package org.sk.sb;

import org.openjdk.jmh.annotations.*;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class ApplicationBenchmark {

    // Using context field instead of Spring annotations
    // See https://github.com/stsypanov/spring-boot-benchmark
    private ConfigurableApplicationContext context;

    @Benchmark
    public Object startUp() {
        return context = SpringApplication.run(Launcher.class);
    }



    @TearDown(Level.Invocation)
    public void close() {
        context.close();
    }
}
