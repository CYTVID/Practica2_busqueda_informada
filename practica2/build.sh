#!/usr/bin/env bash
# build.sh – Compila y ejecuta la Práctica 2
set -e

SRC="src/main/java"
OUT="target/classes"

echo "=== Compilando ==="
mkdir -p "$OUT"
find "$SRC" -name "*.java" | xargs javac -d "$OUT"

echo "=== Ejecutando ==="
java -cp "$OUT" mx.ipn.escom.ia.p2.Main
