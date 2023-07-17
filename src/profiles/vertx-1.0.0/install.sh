#!/bin/sh

git clone https://github.com/eclipse-vertx/vert.x
cd vert.x
git switch 4.4

echo 'diff --git a/src/test/benchmarks/io/vertx/benchmarks/BenchmarkBase.java b/src/test/benchmarks/io/vertx/benchmarks/BenchmarkBase.java
index eb38d6cfb..bca07d933 100644
--- a/src/test/benchmarks/io/vertx/benchmarks/BenchmarkBase.java
+++ b/src/test/benchmarks/io/vertx/benchmarks/BenchmarkBase.java
@@ -29,9 +29,6 @@ import java.util.concurrent.TimeUnit;
 @Threads(1)
 @BenchmarkMode(Mode.Throughput)
 @Fork(value = 1, jvmArgs = {
-    "-XX:+UseBiasedLocking",
-    "-XX:BiasedLockingStartupDelay=0",
-    "-XX:+AggressiveOpts",
     "-Djmh.executor=CUSTOM",
     "-Djmh.executor.class=io.vertx.core.impl.VertxExecutorService"
 })
' >> bench-jvm-version-fix.patch

patch src/test/benchmarks/io/vertx/benchmarks/BenchmarkBase.java < bench-jvm-version-fix.patch

mvn package -DskipTests -Pbenchmarks

cd ..
echo "#!/bin/sh
java -jar vert.x/target/vertx-core-4.4.1-SNAPSHOT-benchmarks.jar -t max > \$LOG_FILE 2>&1" > vertx.sh


