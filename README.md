# Java JVM benchmarks
Benchmarks for Java virtual machine implementations.

Following implementations are tested
- OpenJDK 17 (version "17.0.6" 2023-01-17)
- GraalVM Java 17 (version "17.0.6" 2023-01-17)
- Eclipse J9 (version "17.0.5" 2022-10-18)

Each JVM is latest available version at the time of writing and
targets **Java language release 17** which is a latest Long-Term-Support (LTS) release.

Benchmarks are made using [Phoronix Test Suite](https://www.phoronix-test-suite.com/)
which handles running automated tests as well as results aggregation and report generation.

Detailed instructions about it can be found at its [github page](https://github.com/phoronix-test-suite/phoronix-test-suite).

Benchmarking with PTS is usually made by running a **"test suite"** that contains one or more tests (benchmarks). For example [Java test suite](https://openbenchmarking.org/suite/pts/java) witch contains various Java tests (benchmarks). 

PTS Java test suite allows for testing only one JVM per run, so to test all JVMs the suit will have to be run multiple times with different JVMs and multiple test results will have to be merged to form a single one for comparison.

For initial JVM benchmark we will run default [PTS  Java test suite](https://openbenchmarking.org/suite/pts/java) witch has the following benchmarks:
- Bork File Encrypter
- DaCapo Benchmark
- Java Gradle Build
- Java JMH
- Java SciMark
- Renaissance
- Sunflow Rendering System

## Installing Phoronix Test Suite

PTS is available for Linux and Windows as tar.gz or zip archive package.

Archive can be downloaded for the OS of choice from its [home page](https://www.phoronix-test-suite.com/?k=downloads).

It can be installed or run locally without installation only from the extracted archive file. 

Only prerequisite for running PTS is PHP, full PHP stack is not needed, only PHP command line interface is required.

To start PTS extract archive, change to extracted directory and then run `phoronix-test-suite` from your shell prompt. For installation run `install.sh` on Linux or `install.bat` on Windows.

## How to run the PTS benchmarks

First a JVM than is going to be tested has to be installed and available in shell path variable.

You can check if JVM is properly installed and available on path by typing `java -version` in your shell prompt. 

For example for OpenJ9 JVM.

    $ java -version
    openjdk version "17.0.5" 2022-10-18
    IBM Semeru Runtime Open Edition 17.0.5.0 (build 17.0.5+8)
    Eclipse OpenJ9 VM 17.0.5.0 (build openj9-0.35.0, JRE 17 Linux amd64-64-Bit Compressed References 20221018_325 (JIT enabled, AOT enabled)
    OpenJ9   - e04a7f6c1
    OMR      - 85a21674f
    JCL      - 32d2c409a33 based on jdk-17.0.5+8)

Next to run a benchmark run `phoronix-test-suite benchmark java` which will start Java benchmarks suite.

You will be asked if you want to save test result, select **(Y) yes.**

And choose a name for the result file as well as test configuration name and description,
this can all be changed later when viewing results.

The suite will go through multiple java benchmarks multiple times.
This will take some time (up to 2 hours depending on your machine).

When test suite finishes test results data can be viewed and edited in form of a web page in browser.

Test data can also be exported to pdf and html report files using: 
`phoronix-test-suite result-file-to-pdf {name}` 

or

`phoronix-test-suite result-file-to-pdf {name}`
where `{name}` is name of the result file.

## Benchmark JVM implementations.

Run the test suite as described above for each of JVMs.

Before each test suite run make sure that specific JVM that we want to benchmark is set on shell path to avoid benchmarking the same JVM multiple times.

Some benchmarks can fail on some JVMs for various reasons they will be skipped automatically.

After each separated test suite has completed we can merge results into single result witch allows for comparison between JVMs using command:

`phoronix-test-suite merge-results {result names ...}` where `{result names ...}` are names of test results of the test suites we run earlier.

This will create single result file that can also be exported as described above.

## Custom JVM benchmarks
Currently the following custom benchmark applications are included in this repository for testing.
- spring-benchmark (Simple Spring Boot application for measuring start up time and web page content rendering).
- vertx-benchmark (Set of various benchmarks used in  Vert.x Web framework development, github page (https://github.com/eclipse-vertx/vert.x))

## Custom JVM benchmark suite
A custom phoronix test suite is also availabe as local/java-jvm suite 
which has all benchmarks as phoronix java suite described above plus custom jvm benchmarks.
Results example: https://openbenchmarking.org/result/2305237-NE-RUNLOCALJ70&grw


