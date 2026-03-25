package mx.ipn.escom.ia.p2;

import mx.ipn.escom.ia.p2.puzzle.*;
import mx.ipn.escom.ia.p2.sudoku.*;
import mx.ipn.escom.ia.p2.sudoku.AStarSudokuSolver.DetailedSudokuResult;
import mx.ipn.escom.ia.p2.util.DetailedResult;
import mx.ipn.escom.ia.p2.util.SearchResult;

import java.util.*;

/**
 * Práctica 2 – Búsqueda Informada | IA | ESCOM-IPN
 *
 * Salida en consola con:
 *   · Paso a paso visual del 8-puzzle y 15-puzzle (tableros ASCII).
 *   · Paso a paso del Sudoku con el tablero resaltando cada celda asignada.
 *   · Tablas de comparación de rendimiento.
 */
public class Main {

    static final String RESET  = "\033[0m";
    static final String BOLD   = "\033[1m";
    static final String CYAN   = "\033[36m";
    static final String GREEN  = "\033[32m";
    static final String YELLOW = "\033[33m";
    static final String BLUE   = "\033[34m";
    static final String DIM    = "\033[2m";

    public static void main(String[] args) {
        banner();
        ejercicio1();
        sep();
        ejercicio2();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  EJERCICIO 1 – A* N-Puzzle
    // ══════════════════════════════════════════════════════════════════════
    private static void ejercicio1() {
        title("EJERCICIO 1 – A* para 8-Puzzle y 15-Puzzle");
        AStarPuzzleSolver solver = new AStarPuzzleSolver();

        int[] goal8  = {1,2,3,4,5,6,7,8,0};
        int[] goal15 = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,0};

        int[][] configs8 = {
            {1,2,3,4,5,6,0,7,8},
            {1,2,3,4,0,5,7,8,6},
            {2,8,3,1,6,4,7,0,5},
        };
        String[] labels8 = {"Facil","Media","Dificil"};

        int[][] configs15 = {
            {1,2,3,4,5,6,7,8,9,10,11,12,13,14,0,15},
            {1,2,3,4,5,6,7,8,9,10,11,12,13,0,14,15},
            {1,2,3,4,5,6,7,8,9,10,0,12,13,14,11,15},
        };
        String[] labels15 = {"Facil","Media","Media+"};

        // ── 8-Puzzle paso a paso (Manhattan) ──
        subtitle("8-Puzzle (3x3) - Paso a paso con heuristica Manhattan");
        for (int i = 0; i < configs8.length; i++) {
            PuzzleState init = new PuzzleState(configs8[i], 3, goal8, null, null, 0);
            DetailedResult dr = solver.solveDetailed(init, Heuristic.MANHATTAN);
            printPuzzleStepByStep("8-Puzzle", labels8[i], 3, dr);
        }

        // ── 8-Puzzle tabla comparacion ──
        subtitle("8-Puzzle - Comparacion de las 3 heuristicas");
        printPuzzleCompareHeader();
        for (int i = 0; i < configs8.length; i++) {
            for (Heuristic h : Heuristic.values()) {
                PuzzleState init = new PuzzleState(configs8[i], 3, goal8, null, null, 0);
                SearchResult r = solver.solve(init, h);
                printPuzzleRow("8-puzzle", labels8[i], h, r);
            }
            System.out.println();
        }

        // ── 15-Puzzle paso a paso ──
        subtitle("15-Puzzle (4x4) - Paso a paso con heuristica Manhattan");
        for (int i = 0; i < configs15.length; i++) {
            PuzzleState init = new PuzzleState(configs15[i], 4, goal15, null, null, 0);
            DetailedResult dr = solver.solveDetailed(init, Heuristic.MANHATTAN);
            printPuzzleStepByStep("15-Puzzle", labels15[i], 4, dr);
        }

        // ── 15-Puzzle tabla comparacion ──
        subtitle("15-Puzzle - Comparacion de las 3 heuristicas");
        printPuzzleCompareHeader();
        for (int i = 0; i < configs15.length; i++) {
            for (Heuristic h : Heuristic.values()) {
                PuzzleState init = new PuzzleState(configs15[i], 4, goal15, null, null, 0);
                SearchResult r = solver.solve(init, h);
                printPuzzleRow("15-puzzle", labels15[i], h, r);
            }
            System.out.println();
        }
    }

    /** Muestra el tablero en cada paso side-by-side. */
    private static void printPuzzleStepByStep(String name, String label, int n, DetailedResult dr) {
        System.out.println(CYAN + "  +-- " + name + " [" + label + "] " + RESET);
        if (!dr.isSolved()) {
            System.out.println(YELLOW + "    Sin solucion en este intento." + RESET + "\n");
            return;
        }

        List<int[]> states = dr.getStatePath();
        List<String> moves = dr.getMoveNames();
        int total = states.size();

        System.out.printf("  %sSolucion en %d pasos | %d nodos | %d ms | %d KB%s%n",
                GREEN, total - 1, dr.getNodesExpanded(), dr.getTimeMs(), dr.getMemoryKB(), RESET);
        System.out.println();

        int limit = Math.min(total, 9);
        int groupSize = 4;

        for (int base = 0; base < limit; base += groupSize) {
            int end = Math.min(base + groupSize, limit);
            int colW = (n == 3) ? 12 : 18;

            // Cabecera de pasos
            StringBuilder header = new StringBuilder("  ");
            for (int s = base; s < end; s++) {
                String stepLabel;
                if (s == 0) stepLabel = "Inicio";
                else if (s == total - 1) stepLabel = "META OK";
                else stepLabel = "Paso " + s + "(" + moves.get(s) + ")";
                header.append(String.format("%-" + colW + "s", stepLabel));
                if (s < end - 1) header.append("  ->  ");
            }
            System.out.println(header);

            // Render side-by-side
            String[][] renders = new String[end - base][];
            for (int s = base; s < end; s++) {
                renders[s - base] = renderPuzzleTiles(states.get(s), n,
                        s > 0 ? states.get(s - 1) : null);
            }
            int rowCount = renders[0].length;
            for (int row = 0; row < rowCount; row++) {
                StringBuilder line = new StringBuilder("  ");
                for (int s = 0; s < renders.length; s++) {
                    line.append(renders[s][row]);
                    if (s < renders.length - 1) line.append("       ");
                }
                System.out.println(line);
            }
            System.out.println();
        }

        if (total > 9) {
            System.out.println(DIM + "  ... (" + (total - 9) + " pasos adicionales omitidos)" + RESET);
        }
        System.out.println();
    }

    /** Renderiza tablero N-puzzle como array de filas de texto. */
    private static String[] renderPuzzleTiles(int[] tiles, int n, int[] prev) {
        String hLine = "+" + ("---+").repeat(n);
        List<String> rows = new ArrayList<>();
        rows.add(hLine);
        for (int r = 0; r < n; r++) {
            StringBuilder row = new StringBuilder("|");
            for (int c = 0; c < n; c++) {
                int v = tiles[r * n + c];
                boolean changed = prev != null && prev[r * n + c] != v;
                String cell;
                if (v == 0) {
                    cell = "   ";
                } else if (changed) {
                    cell = String.format(YELLOW + "%2d " + RESET, v);
                } else {
                    cell = String.format("%2d ", v);
                }
                row.append(cell).append("|");
            }
            rows.add(row.toString());
        }
        rows.add(hLine);
        return rows.toArray(new String[0]);
    }

    private static void printPuzzleCompareHeader() {
        System.out.printf("  %-10s %-8s %-12s %8s %10s %10s %6s%n",
                "Puzzle","Dific.","Heuristica","Nodos","Tiempo(ms)","Mem(KB)","Pasos");
        System.out.println("  " + "-".repeat(68));
    }

    private static void printPuzzleRow(String puzzle, String label, Heuristic h, SearchResult r) {
        String hName = switch (h) {
            case MISPLACED -> "Fuera lugar";
            case MANHATTAN -> "Manhattan  ";
            case CUSTOM    -> "Personaliz.";
        };
        String steps = r.isSolved()
                ? r.getSolutionDesc().split("->")[0].replace("Pasos: ","").trim()
                : "N/A";
        System.out.printf("  %-10s %-8s %-12s %8d %10d %10d %6s%n",
                puzzle, label, hName,
                r.getNodesExpanded(), r.getTimeMs(), r.getMemoryKB(), steps);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  EJERCICIO 2 – Sudoku
    // ══════════════════════════════════════════════════════════════════════
    private static void ejercicio2() {
        title("EJERCICIO 2 – Sudoku: A* vs. Recocido Simulado");

        Random rng = new Random(42);
        AStarSudokuSolver astar = new AStarSudokuSolver();
        SimulatedAnnealingSolver sa = new SimulatedAnnealingSolver();

        int[] emptyCounts = {20, 35, 45};
        String[] levels   = {"Facil (20 vacias)", "Intermedio (35 vacias)", "Dificil (45 vacias)"};

        for (int lvl = 0; lvl < 3; lvl++) {
            SudokuBoard board = SudokuBoard.generate(emptyCounts[lvl], new Random(rng.nextLong()));

            subtitle("Nivel: " + levels[lvl]);

            // Tablero inicial
            System.out.println(BLUE + "  Tablero inicial (puntos = celdas vacias):" + RESET);
            printSudoku(board, null);

            // A* con paso a paso
            System.out.println(CYAN + "\n  -- A* (con MRV) --" + RESET);
            DetailedSudokuResult dsr = astar.solveDetailed(board.copy());
            SearchResult ra = dsr.result();

            printSudokuStepByStep(dsr.steps(), emptyCounts[lvl]);

            System.out.printf("  %sResultado A*:%s  Nodos: %d | Tiempo: %d ms | Mem: %d KB | %s%n%n",
                    GREEN, RESET,
                    ra.getNodesExpanded(), ra.getTimeMs(), ra.getMemoryKB(),
                    ra.isSolved() ? GREEN + "OK Resuelto" + RESET : YELLOW + "NO Resuelto" + RESET);

            if (ra.isSolved() && !dsr.steps().isEmpty()) {
                System.out.println(GREEN + "  Tablero resuelto (A*):" + RESET);
                printSudoku(dsr.steps().get(dsr.steps().size()-1).board, null);
            }

            // Recocido Simulado
            System.out.println(CYAN + "\n  -- Recocido Simulado --" + RESET);
            SearchResult rs = sa.solve(board.copy(), 42L + lvl);

            System.out.printf("  %sResultado SA:%s   Iters: %d | Tiempo: %d ms | Mem: %d KB | %s%n",
                    GREEN, RESET,
                    rs.getNodesExpanded(), rs.getTimeMs(), rs.getMemoryKB(),
                    rs.isSolved() ? GREEN + "OK Resuelto" + RESET : YELLOW + "NO Resuelto" + RESET);
            System.out.println("  " + DIM + rs.getSolutionDesc() + RESET + "\n");

            // Tabla comparativa del nivel
            System.out.println("  Comparacion para este nivel:");
            System.out.printf("  %-16s %10s %12s %10s %8s%n",
                    "Algoritmo","Nodos/Iters","Tiempo(ms)","Mem(KB)","OK?");
            System.out.println("  " + "-".repeat(58));
            System.out.printf("  %-16s %10d %12d %10d %8s%n",
                    "A*", ra.getNodesExpanded(), ra.getTimeMs(), ra.getMemoryKB(),
                    ra.isSolved() ? "OK" : "--");
            System.out.printf("  %-16s %10d %12d %10d %8s%n%n",
                    "Rec. Simulado", rs.getNodesExpanded(), rs.getTimeMs(), rs.getMemoryKB(),
                    rs.isSolved() ? "OK" : "--");
        }

        // Tabla global
        subtitle("Tabla Global – A* vs Recocido Simulado");
        System.out.printf("  %-23s %-15s %8s %10s %10s %6s%n",
                "Nivel","Algoritmo","Nodos","Tiempo(ms)","Mem(KB)","OK?");
        System.out.println("  " + "-".repeat(74));
        rng = new Random(42);
        for (int lvl = 0; lvl < 3; lvl++) {
            SudokuBoard board = SudokuBoard.generate(emptyCounts[lvl], new Random(rng.nextLong()));
            SearchResult ra = astar.solve(board.copy());
            SearchResult rs = sa.solve(board.copy(), 42L + lvl);
            System.out.printf("  %-23s %-15s %8d %10d %10d %6s%n",
                    levels[lvl], "A*",
                    ra.getNodesExpanded(), ra.getTimeMs(), ra.getMemoryKB(),
                    ra.isSolved() ? "OK" : "--");
            System.out.printf("  %-23s %-15s %8d %10d %10d %6s%n%n",
                    "", "Rec. Simulado",
                    rs.getNodesExpanded(), rs.getTimeMs(), rs.getMemoryKB(),
                    rs.isSolved() ? "OK" : "--");
        }
    }

    /** Muestra tableros en el paso a paso del Sudoku cada cierto intervalo. */
    private static void printSudokuStepByStep(List<SudokuStep> steps, int totalEmpty) {
        if (steps.isEmpty()) { System.out.println("  (sin pasos registrados)\n"); return; }

        int show = Math.min(6, steps.size());
        int interval = Math.max(1, steps.size() / show);

        System.out.printf("  Mostrando %d snapshots de %d asignaciones:%n%n", show, steps.size());

        int shown = 0;
        for (int i = 0; i < steps.size() && shown < show; i++) {
            boolean isLast = (i == steps.size() - 1);
            if (i % interval != 0 && !isLast) continue;

            SudokuStep step = steps.get(i);
            Set<String> hl = new HashSet<>();
            hl.add(step.row + "," + step.col);

            System.out.printf("  Asignacion %d/%d  Celda [F%d,C%d] = %s%d%s%n",
                    i + 1, steps.size(),
                    step.row + 1, step.col + 1,
                    YELLOW, step.value, RESET);

            String[] boardLines = step.board.toStringHighlighted(hl).split("\n");
            for (String line : boardLines)
                System.out.println("    " + line);
            System.out.println();
            shown++;
        }
    }

    /** Imprime el tablero Sudoku con indentacion. */
    private static void printSudoku(SudokuBoard board, Set<String> highlight) {
        String[] lines = board.toStringHighlighted(highlight).split("\n");
        for (String line : lines)
            System.out.println("    " + line);
    }

    // ── Formato ──────────────────────────────────────────────────────────
    private static void banner() {
        System.out.println(BOLD + CYAN);
        System.out.println("+==========================================================+");
        System.out.println("|    PRACTICA 2 - Busqueda Informada  |  IA  |  ESCOM      |");
        System.out.println("|    Instituto Politecnico Nacional                         |");
        System.out.println("+==========================================================+");
        System.out.println(RESET);
    }

    private static void title(String t) {
        System.out.println(BOLD + BLUE +
                "\n==========================================================");
        System.out.println("  " + t);
        System.out.println("==========================================================" + RESET + "\n");
    }

    private static void subtitle(String t) {
        System.out.println(BOLD + "\n  > " + t + RESET);
        System.out.println("  " + "-".repeat(54));
    }

    private static void sep() {
        System.out.println(DIM + "\n" + "=".repeat(60) + RESET + "\n");
    }
}
