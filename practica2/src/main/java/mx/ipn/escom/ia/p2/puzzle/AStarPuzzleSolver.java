package mx.ipn.escom.ia.p2.puzzle;

import mx.ipn.escom.ia.p2.util.DetailedResult;
import mx.ipn.escom.ia.p2.util.MemoryUtil;
import mx.ipn.escom.ia.p2.util.SearchResult;

import java.util.*;

/**
 * Implementación de A* para el N-puzzle (8-puzzle y 15-puzzle).
 *
 * <p>Utiliza una cola de prioridad (min-heap) ordenada por f(n) = g(n) + h(n).
 * Para evitar expansiones redundantes se mantiene un mapa de mejor g
 * conocido por estado (closed list con re-apertura si se encuentra un
 * camino más barato).</p>
 */
public class AStarPuzzleSolver {

    /**
     * Resuelve el puzzle dado con la heurística indicada.
     *
     * @param initial   estado inicial
     * @param heuristic heurística a usar
     * @return SearchResult con métricas de rendimiento
     */
    public SearchResult solve(PuzzleState initial, Heuristic heuristic) {
        MemoryUtil mem = new MemoryUtil();
        mem.start();
        long t0 = System.currentTimeMillis();

        // Cola de prioridad ordenada por f = g + h
        PriorityQueue<PuzzleState> open = new PriorityQueue<>(
                Comparator.comparingInt(s -> s.f(heuristic)));
        // Mejor g conocido por estado
        Map<PuzzleState, Integer> bestG = new HashMap<>();

        open.add(initial);
        bestG.put(initial, 0);
        int expanded = 0;

        while (!open.isEmpty()) {
            PuzzleState cur = open.poll();

            if (cur.isGoal()) {
                long elapsed = System.currentTimeMillis() - t0;
                long kb = mem.elapsedKB();
                List<String> path = cur.path();
                String desc = "Pasos: " + path.size() + " -> " + path;
                return new SearchResult(true, expanded, elapsed, kb, desc);
            }

            // Omitir si ya encontramos un camino más barato al mismo estado
            if (bestG.containsKey(cur) && cur.getG() > bestG.get(cur)) continue;

            expanded++;

            for (PuzzleState next : cur.successors()) {
                int known = bestG.getOrDefault(next, Integer.MAX_VALUE);
                if (next.getG() < known) {
                    bestG.put(next, next.getG());
                    open.add(next);
                }
            }
        }

        long elapsed = System.currentTimeMillis() - t0;
        long kb = mem.elapsedKB();
        return new SearchResult(false, expanded, elapsed, kb, "Sin solución");
    }

    /**
     * Igual que solve() pero devuelve un DetailedResult con la secuencia
     * completa de estados (tableros) y nombres de movimientos para mostrar
     * el paso a paso en consola.
     */
    public DetailedResult solveDetailed(PuzzleState initial, Heuristic heuristic) {
        MemoryUtil mem = new MemoryUtil();
        mem.start();
        long t0 = System.currentTimeMillis();

        PriorityQueue<PuzzleState> open = new PriorityQueue<>(
                Comparator.comparingInt(s -> s.f(heuristic)));
        Map<PuzzleState, Integer> bestG = new HashMap<>();

        open.add(initial);
        bestG.put(initial, 0);
        int expanded = 0;

        while (!open.isEmpty()) {
            PuzzleState cur = open.poll();

            if (cur.isGoal()) {
                long elapsed = System.currentTimeMillis() - t0;
                long kb = mem.elapsedKB();

                // Reconstruir la secuencia completa de estados
                List<PuzzleState> stateSeq = new ArrayList<>();
                PuzzleState node = cur;
                while (node != null) {
                    stateSeq.add(0, node);
                    node = node.getParent();
                }

                List<int[]> tiles = new ArrayList<>();
                List<String> moves = new ArrayList<>();
                for (int i = 0; i < stateSeq.size(); i++) {
                    tiles.add(stateSeq.get(i).getTiles());
                    moves.add(stateSeq.get(i).getMove() == null ? "Inicio" : stateSeq.get(i).getMove());
                }

                String desc = "Pasos: " + cur.path().size() + " -> " + cur.path();
                return new DetailedResult(true, expanded, elapsed, kb, desc, tiles, moves);
            }

            if (bestG.containsKey(cur) && cur.getG() > bestG.get(cur)) continue;
            expanded++;

            for (PuzzleState next : cur.successors()) {
                int known = bestG.getOrDefault(next, Integer.MAX_VALUE);
                if (next.getG() < known) {
                    bestG.put(next, next.getG());
                    open.add(next);
                }
            }
        }

        long elapsed = System.currentTimeMillis() - t0;
        return new DetailedResult(false, expanded, elapsed, mem.elapsedKB(),
                "Sin solución", List.of(), List.of());
    }
}
