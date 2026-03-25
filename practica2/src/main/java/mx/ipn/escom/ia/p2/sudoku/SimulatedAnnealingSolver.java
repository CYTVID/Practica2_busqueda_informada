package mx.ipn.escom.ia.p2.sudoku;

import mx.ipn.escom.ia.p2.util.MemoryUtil;
import mx.ipn.escom.ia.p2.util.SearchResult;

import java.util.*;

/**
 * Resuelve Sudoku con Recocido Simulado (Simulated Annealing).
 *
 * <p><b>Representación del estado:</b> cada caja 3×3 se llena con los dígitos
 * 1-9 de forma que no haya repeticiones dentro de la caja (restricción de
 * caja siempre satisfecha). Las únicas violaciones provienen de filas y
 * columnas.</p>
 *
 * <p><b>Función de coste:</b> número total de conflictos en filas y columnas.</p>
 *
 * <p><b>Movimiento:</b> intercambiar dos celdas vacías dentro de la misma caja.</p>
 *
 * <p><b>Parámetros:</b>
 * <ul>
 *   <li>Temperatura inicial T0 = 1.0</li>
 *   <li>Factor de enfriamiento α = 0.9999</li>
 *   <li>Iteraciones máximas: 500 000</li>
 *   <li>Reinicio si no hay mejora en 1000 iteraciones</li>
 * </ul>
 * </p>
 */
public class SimulatedAnnealingSolver {

    private static final double T0      = 1.0;
    private static final double ALPHA   = 0.9999;
    private static final int    MAX_IT  = 500_000;
    private static final int    NO_IMP  = 1_000;

    public SearchResult solve(SudokuBoard initial, long seed) {
        MemoryUtil mem = new MemoryUtil();
        mem.start();
        long t0 = System.currentTimeMillis();

        Random rng = new Random(seed);

        // Celdas fijas (pistas del puzzle)
        boolean[][] fixed = new boolean[9][9];
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++)
                fixed[r][c] = initial.get(r, c) != 0;

        // Inicializar: llenar cada caja con dígitos faltantes aleatoriamente
        SudokuBoard cur = initial.copy();
        fillBoxes(cur, fixed, rng);

        int curCost = cur.countConflicts();
        SudokuBoard best = cur.copy();
        int bestCost = curCost;

        double temp = T0;
        int noImprove = 0;
        int iterations = 0;

        while (iterations < MAX_IT && bestCost > 0) {
            // Elegir caja aleatoria
            int br = rng.nextInt(3) * 3;
            int bc = rng.nextInt(3) * 3;

            // Obtener celdas no fijas en esa caja
            List<int[]> free = new ArrayList<>();
            for (int dr = 0; dr < 3; dr++)
                for (int dc = 0; dc < 3; dc++) {
                    int r = br + dr, c = bc + dc;
                    if (!fixed[r][c]) free.add(new int[]{r, c});
                }

            if (free.size() < 2) { iterations++; temp *= ALPHA; continue; }

            // Intercambiar dos celdas libres al azar
            int i1 = rng.nextInt(free.size()), i2;
            do { i2 = rng.nextInt(free.size()); } while (i2 == i1);

            int[] p1 = free.get(i1), p2 = free.get(i2);
            int v1 = cur.get(p1[0], p1[1]);
            int v2 = cur.get(p2[0], p2[1]);

            cur.set(p1[0], p1[1], v2);
            cur.set(p2[0], p2[1], v1);
            int newCost = cur.countConflicts();

            int delta = newCost - curCost;
            if (delta < 0 || rng.nextDouble() < Math.exp(-delta / temp)) {
                curCost = newCost;
                if (curCost < bestCost) {
                    bestCost = curCost;
                    best = cur.copy();
                    noImprove = 0;
                }
            } else {
                // Revertir
                cur.set(p1[0], p1[1], v1);
                cur.set(p2[0], p2[1], v2);
            }

            temp *= ALPHA;
            iterations++;
            noImprove++;

            // Reinicio aleatorio si estancado
            if (noImprove >= NO_IMP) {
                cur = best.copy();
                perturbBoxes(cur, fixed, rng);
                curCost = cur.countConflicts();
                temp = T0 * 0.5;
                noImprove = 0;
            }
        }

        long elapsed = System.currentTimeMillis() - t0;
        boolean solved = best.isSolved();
        String desc = String.format("Iteraciones: %d | Conflictos finales: %d", iterations, bestCost);
        return new SearchResult(solved, iterations, elapsed, mem.elapsedKB(), desc);
    }

    /** Rellena cada caja con los dígitos faltantes mezclados aleatoriamente. */
    private void fillBoxes(SudokuBoard b, boolean[][] fixed, Random rng) {
        for (int br = 0; br < 3; br++) {
            for (int bc = 0; bc < 3; bc++) {
                Set<Integer> present = new HashSet<>();
                List<int[]> empty = new ArrayList<>();
                for (int dr = 0; dr < 3; dr++) {
                    for (int dc = 0; dc < 3; dc++) {
                        int r = br * 3 + dr, c = bc * 3 + dc;
                        if (fixed[r][c]) present.add(b.get(r, c));
                        else empty.add(new int[]{r, c});
                    }
                }
                List<Integer> missing = new ArrayList<>();
                for (int v = 1; v <= 9; v++) if (!present.contains(v)) missing.add(v);
                Collections.shuffle(missing, rng);
                for (int k = 0; k < empty.size(); k++)
                    b.set(empty.get(k)[0], empty.get(k)[1], missing.get(k));
            }
        }
    }

    /** Perturbación: rebaraja las celdas libres dentro de varias cajas. */
    private void perturbBoxes(SudokuBoard b, boolean[][] fixed, Random rng) {
        fillBoxes(b, fixed, rng); // Simple: llenar de nuevo aleatoriamente
    }
}
