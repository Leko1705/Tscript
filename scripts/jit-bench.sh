#!/bin/bash
# Benchmarks the JIT against the interpreter.
#
# usage: scripts/jit-bench.sh [-r runs] [-o "jvm options"] [-n] [programs...]
#   -r runs   number of runs per program and mode (median is reported), default 3
#   -o opts   additional JVM options for the JIT run (e.g. "-Dtscript.jit.threshold2=500")
#   -n        skip the interpreter run (only measure the JIT)
# Programs are taken from src/test/resources/bench. Times include JVM startup.
# Build first with ./gradlew compileJava (or set CLASSES to another class directory).

ROOT=$(cd "$(dirname "$0")/.." && pwd)
RUNS=3
OPTS=""
SKIP_INTERPRETER=0
while getopts "r:o:n" opt; do
  case $opt in
    r) RUNS=$OPTARG ;;
    o) OPTS=$OPTARG ;;
    n) SKIP_INTERPRETER=1 ;;
    *) exit 1 ;;
  esac
done
shift $((OPTIND - 1))

CP="${CLASSES:-$ROOT/build/classes/java/main}:$(find ~/.gradle/caches -name '*.jar' | grep -E 'picocli-4.7.6|/asm-6.0|asm-util-6.0|asm-tree-6.0' | tr '\n' ':')"
SRC="$ROOT/src/test/resources/bench"
WORK=$(mktemp -d)
trap 'rm -rf "$WORK"' EXIT

programs="$@"
[ -z "$programs" ] && programs=$(cd "$SRC" && ls *.tscript | sed 's/.tscript//')

run() { # name mode opts -> prints "seconds output"
  local d="$WORK/$1-$2"
  rm -rf "$d"; mkdir -p "$d/src" "$d/out"
  cp "$SRC/$1.tscript" "$d/src/"
  printf "src $d/src\nout $d/out\nrun $1\njit $2\n" > "$d/project.tsrt"
  local start end out
  start=$(date +%s%N)
  out=$(cd "$d" && java $3 -cp "$CP" com.tscript.cli.tscript -p "$d/project.tsrt" 2>&1 | grep -v JAVA_TOOL | tail -2 | tr '\n' ' ')
  end=$(date +%s%N)
  echo "$(( (end - start) / 1000000 )) $out"
}

median() { sort -n | awk '{a[NR]=$1} END {print a[int((NR+1)/2)]}'; }

printf "%-12s %10s %10s %8s\n" program interp_ms jit_ms speedup
for p in $programs; do
  ref=""; times_off=""; times_on=""; ok="ok"
  for ((i = 0; i < RUNS; i++)); do
    if [ $SKIP_INTERPRETER = 0 ]; then
      r=$(run "$p" off ""); times_off="$times_off ${r%% *}"; ref="${r#* }"
    fi
    r=$(run "$p" on "$OPTS"); times_on="$times_on ${r%% *}"
    [ -n "$ref" ] && [ "${r#* }" != "$ref" ] && ok="OUTPUT DIFFERS: '${r#* }' vs '$ref'"
  done
  on=$(echo $times_on | tr ' ' '\n' | median)
  if [ $SKIP_INTERPRETER = 0 ]; then
    off=$(echo $times_off | tr ' ' '\n' | median)
    printf "%-12s %10d %10d %7.2fx %s\n" "$p" "$off" "$on" "$(echo "$off / $on" | bc -l)" "$([ "$ok" = ok ] || echo "$ok")"
  else
    printf "%-12s %10s %10d\n" "$p" "-" "$on"
  fi
done
