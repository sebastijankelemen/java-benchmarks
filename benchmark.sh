#!/bin/bash
# Bash script for benchmarking various JVM platforms
echo JVM benchmarks using Renaissance Suite tool
echo Using java version: {$1} -version
for value in als chi-square dec-tree gauss-mix log-regression movie-lens naive-bayes page-rank akka-uct fj-kmeans reactors db-shootout neo4j-analytics future-genetic mnemonics par-mnemonics rx-scrabble scrabble dotty philosophers scala-doku scala-kmeans scala-stm-bench7 finagle-chirper finagle-http;
do
    $1 -jar $2 $value --json  $value.json
done