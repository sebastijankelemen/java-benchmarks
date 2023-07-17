#!/bin/sh
java -jar vert.x/target/vertx-core-4.4.1-SNAPSHOT-benchmarks.jar -t max > $LOG_FILE 2>&1
