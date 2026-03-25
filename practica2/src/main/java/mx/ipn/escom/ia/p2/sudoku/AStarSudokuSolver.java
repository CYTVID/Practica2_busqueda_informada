package mx.ipn.escom.ia.p2.sudoku;

import mx.ipn.escom.ia.p2.util.MemoryUtil;
import mx.ipn.escom.ia.p2.util.SearchResult;

import java.util.*;

/**
 * Resuelve Sudoku con A* sobre el espacio de asignación de celdas.
 *
 * <p>Heurística (admisible): número de celdas vacías restantes.
 * Cada asignación de una celda vacía reduce el costo restante en 1,
 * por lo que h(n) = celdas vacías nunca sobreestima el costo real.</p>
 *
 * <p>Para mejorar la eficiencia se aplica la heurística MRV (Minimum
 * Remaining Values): siempre se elige la celda vacía con el menor
 * número de valores legales disponibles.</p>
 */
public class AStarSudokuSolver {

    public SearchResult solve(SudokuBoard initial) {
        return solveDetailed(initial).result;
    }

    /**
     * Resuelve el Sudoku y captura cada asignación como un SudokuStep
     * para poder mostrar el paso a paso en consola.
     */
    public DetailedSudokuResult solveDetailed(SudokuBoard initial) {
        MemoryUtil mem = new MemoryUtil();
        mem.start();
        long t0 = System.currentTimeMillis();

        // Estado: tablero + g (celdas asignadas) + historial de pasos
        record State(SudokuBoard board, int g, List<SudokuStep> steps) {}

        Comparator<State> cmp = Comparator.comparingInt(s -> s.g() + countEmpty(s.board()));
        PriorityQueue<State> open = new PriorityQueue<>(cmp);
        open.add(new State(initial.copy(), 0, new ArrayList<>()));

        int expanded = 0;

        while (!open.isEmpty()) {
            State cur = open.poll();
            expanded++;

            if (cur.board().isSolved()) {
                long elapsed = System.currentTimeMillis() - t0;
                String desc = "Resuelto con A* en " + expanded + " nodos expandidos.";
                SearchResult r = new SearchResult(true, expanded, elapsed, mem.elapsedKB(), desc);
                return new DetailedSudokuResult(r, cur.steps());
            }

            // MRV: celda vacía con menos opciones legales
            int bestR = -1, bestC = -1, bestCount = 10;
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (cur.board().get(r, c) == 0) {
                        int opts = options(cur.board(), r, c);
                        if (opts < bestCount) {
                            bestCount = opts;
                            bestR = r;
                            bestC = c;
                        }
                    }
                }
            }

            if (bestR == -1) continue;

            for (int v = 1; v <= 9; v++) {
                if (cur.board().isValid(bestR, bestC, v)) {
                    SudokuBoard next = cur.board().copy();
                    next.set(bestR, bestC, v);
                    List<SudokuStep> nextSteps = new ArrayList<>(cur.steps());
                    nextSteps.add(new SudokuStep(bestR, bestC, v, next.copy()));
                    open.add(new State(next, cur.g() + 1, nextSteps));
                }
            }
        }

        long elapsed = System.currentTimeMillis() - t0;
        SearchResult r = new SearchResult(false, expanded, elapsed, mem.elapsedKB(), "Sin solución");
        return new DetailedSudokuResult(r, List.of());
    }

    private int countEmpty(SudokuBoard b) {
        int cnt = 0;
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++)
                if (b.get(r, c) == 0) cnt++;
        return cnt;
    }

    private int options(SudokuBoard b, int r, int c) {
        int cnt = 0;
        for (int v = 1; v <= 9; v++)
            if (b.isValid(r, c, v)) cnt++;
        return cnt;
    }

    /** Contenedor de resultado + secuencia de pasos. */
    public record DetailedSudokuResult(SearchResult result, List<SudokuStep> steps) {}
}

