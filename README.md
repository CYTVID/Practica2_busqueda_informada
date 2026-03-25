
# Práctica 2 – Búsqueda Informada
**Inteligencia Artificial | ESCOM-IPN**

## Integrantes
- Carachure Pérez Alexis Saúl
- Gutiérrez Manzano Víctor Hugo
- Olivares Cruz Victor Manuel
- Ramírez García Juan Carlos
- Rangel Mata Jose Luis

## Descripción
Implementación en Java (consola) del algoritmo **A\*** y **Recocido Simulado** para:

- **Ejercicio 1:** 8-Puzzle y 15-Puzzle con tres heurísticas:
  - Fichas fuera de lugar
  - Distancia Manhattan
  - Heurística personalizada (Manhattan + conflictos lineales)

- **Ejercicio 2:** Sudoku en tres niveles de dificultad (20, 35 y 45 celdas vacías) comparando A* vs. Recocido Simulado.

## Requisitos
- **Java 21** o superior (JDK)

## Compilar y ejecutar
```bash
# Linux / macOS
chmod +x build.sh
./build.sh

# Windows (PowerShell)
javac -d target/classes (Get-ChildItem -Recurse src\main\java -Filter "*.java").FullName
java -cp target/classes mx.ipn.escom.ia.p2.Main
```

## Estructura del proyecto
```
src/main/java/mx/ipn/escom/ia/p2/
├── Main.java                       # Punto de entrada, experimentos en consola
├── util/
│   ├── SearchResult.java           # Encapsula métricas de rendimiento
│   └── MemoryUtil.java             # Medición de consumo de memoria heap
├── puzzle/
│   ├── Heuristic.java              # Enum de heurísticas (MISPLACED, MANHATTAN, CUSTOM)
│   ├── PuzzleState.java            # Estado inmutable del N-puzzle con heurísticas
│   └── AStarPuzzleSolver.java      # A* para 8-puzzle y 15-puzzle
└── sudoku/
    ├── SudokuBoard.java            # Tablero 9×9, validación, generación, conflictos
    ├── AStarSudokuSolver.java      # A* con MRV para Sudoku
    └── SimulatedAnnealingSolver.java # Recocido Simulado para Sudoku
```

## Notas de diseño
- **PuzzleState** es inmutable: cada movimiento genera un nuevo objeto, evitando efectos secundarios en la búsqueda.
- **A* para Sudoku** usa la heurística MRV (Minimum Remaining Values) para elegir la celda con menos opciones legales — reduce dramáticamente el factor de ramificación efectivo.
- **Recocido Simulado** trabaja sobre cajas 3×3 completas (restricción de caja siempre satisfecha). Los movimientos intercambian dos celdas libres dentro de la misma caja. Implementa reinicio aleatorio ante estancamiento.
- La medición de memoria usa `Runtime.totalMemory() - Runtime.freeMemory()` con GC previo para minimizar ruido.
