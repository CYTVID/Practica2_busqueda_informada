package mx.ipn.escom.ia.p2.puzzle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Estado de un N-puzzle cuadrado (3×3 = 8-puzzle, 4×4 = 15-puzzle).
 * El valor 0 representa la casilla vacía.
 * Inmutable: cada movimiento genera un nuevo objeto.
 */
public class PuzzleState {

    /** Tiles aplanados en fila mayor. */
    private final int[] tiles;
    /** Lado del tablero (3 para 8-puzzle, 4 para 15-puzzle). */
    private final int n;
    /** Posición de la casilla vacía. */
    private final int blankPos;
    /** Estado objetivo para heurísticas. */
    private final int[] goal;

    // Búsqueda del camino
    private final PuzzleState parent;
    private final String move;

    /** Costo acumulado g(n) desde el estado inicial. */
    private final int g;

    public PuzzleState(int[] tiles, int n, int[] goal, PuzzleState parent, String move, int g) {
        this.tiles    = Arrays.copyOf(tiles, tiles.length);
        this.n        = n;
        this.goal     = goal;
        this.parent   = parent;
        this.move     = move;
        this.g        = g;
        int pos = 0;
        for (int i = 0; i < tiles.length; i++) {
            if (tiles[i] == 0) { pos = i; break; }
        }
        this.blankPos = pos;
    }

    /** Genera los estados sucesores moviendo la casilla vacía. */
    public List<PuzzleState> successors() {
        List<PuzzleState> list = new ArrayList<>();
        int r = blankPos / n;
        int c = blankPos % n;

        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        String[] names = {"Arriba","Abajo","Izquierda","Derecha"};

        for (int d = 0; d < 4; d++) {
            int nr = r + dirs[d][0];
            int nc = c + dirs[d][1];
            if (nr >= 0 && nr < n && nc >= 0 && nc < n) {
                int[] next = Arrays.copyOf(tiles, tiles.length);
                int swapPos = nr * n + nc;
                next[blankPos] = next[swapPos];
                next[swapPos] = 0;
                list.add(new PuzzleState(next, n, goal, this, names[d], g + 1));
            }
        }
        return list;
    }

    /** Heurística: fichas fuera de lugar (sin contar el hueco). */
    public int hMisplaced() {
        int count = 0;
        for (int i = 0; i < tiles.length; i++) {
            if (tiles[i] != 0 && tiles[i] != goal[i]) count++;
        }
        return count;
    }

    /** Heurística: distancia Manhattan de cada ficha a su posición meta. */
    public int hManhattan() {
        int sum = 0;
        for (int i = 0; i < tiles.length; i++) {
            if (tiles[i] == 0) continue;
            // Buscar la posición de goal para tiles[i]
            for (int j = 0; j < goal.length; j++) {
                if (goal[j] == tiles[i]) {
                    sum += Math.abs(i / n - j / n) + Math.abs(i % n - j % n);
                    break;
                }
            }
        }
        return sum;
    }

    /**
     * Heurística personalizada: Manhattan + 2 * conflictos lineales.
     * Un conflicto lineal existe cuando dos fichas están en su fila/columna
     * meta pero en el orden incorrecto — cada conflicto añade al menos 2 movimientos.
     * Es admisible porque nunca sobreestima el costo real.
     */
    public int hCustom() {
        int base = hManhattan();
        int conflicts = 0;

        // Conflictos en filas
        for (int row = 0; row < n; row++) {
            for (int c1 = 0; c1 < n - 1; c1++) {
                int t1 = tiles[row * n + c1];
                if (t1 == 0) continue;
                // Fila meta de t1
                int goalRow1 = -1;
                for (int j = 0; j < goal.length; j++) {
                    if (goal[j] == t1) { goalRow1 = j / n; break; }
                }
                if (goalRow1 != row) continue;
                for (int c2 = c1 + 1; c2 < n; c2++) {
                    int t2 = tiles[row * n + c2];
                    if (t2 == 0) continue;
                    int goalRow2 = -1;
                    int goalCol2 = -1;
                    for (int j = 0; j < goal.length; j++) {
                        if (goal[j] == t2) { goalRow2 = j / n; goalCol2 = j % n; break; }
                    }
                    if (goalRow2 != row) continue;
                    // c1 < c2 pero posición goal de t1 > goal col de t2
                    int goalCol1 = -1;
                    for (int j = 0; j < goal.length; j++) {
                        if (goal[j] == t1) { goalCol1 = j % n; break; }
                    }
                    if (goalCol1 > goalCol2) conflicts++;
                }
            }
        }

        // Conflictos en columnas
        for (int col = 0; col < n; col++) {
            for (int r1 = 0; r1 < n - 1; r1++) {
                int t1 = tiles[r1 * n + col];
                if (t1 == 0) continue;
                int goalCol1 = -1;
                for (int j = 0; j < goal.length; j++) {
                    if (goal[j] == t1) { goalCol1 = j % n; break; }
                }
                if (goalCol1 != col) continue;
                for (int r2 = r1 + 1; r2 < n; r2++) {
                    int t2 = tiles[r2 * n + col];
                    if (t2 == 0) continue;
                    int goalCol2 = -1;
                    int goalRow2 = -1;
                    for (int j = 0; j < goal.length; j++) {
                        if (goal[j] == t2) { goalCol2 = j % n; goalRow2 = j / n; break; }
                    }
                    if (goalCol2 != col) continue;
                    int goalRow1 = -1;
                    for (int j = 0; j < goal.length; j++) {
                        if (goal[j] == t1) { goalRow1 = j / n; break; }
                    }
                    if (goalRow1 > goalRow2) conflicts++;
                }
            }
        }

        return base + 2 * conflicts;
    }

    public int h(Heuristic heuristic) {
        return switch (heuristic) {
            case MISPLACED -> hMisplaced();
            case MANHATTAN -> hManhattan();
            case CUSTOM    -> hCustom();
        };
    }

    public int f(Heuristic heuristic) { return g + h(heuristic); }

    public boolean isGoal() { return Arrays.equals(tiles, goal); }

    /** Reconstruye la lista de movimientos desde el estado inicial. */
    public List<String> path() {
        List<String> moves = new ArrayList<>();
        PuzzleState cur = this;
        while (cur.parent != null) {
            moves.add(0, cur.move);
            cur = cur.parent;
        }
        return moves;
    }

    /** Verifica si una configuración es resoluble (conteo de inversiones). */
    public static boolean isSolvable(int[] tiles, int n) {
        int inversions = 0;
        for (int i = 0; i < tiles.length - 1; i++) {
            if (tiles[i] == 0) continue;
            for (int j = i + 1; j < tiles.length; j++) {
                if (tiles[j] == 0) continue;
                if (tiles[i] > tiles[j]) inversions++;
            }
        }
        if (n % 2 == 1) {
            return inversions % 2 == 0;
        } else {
            // Para tableros de lado par: fila del hueco desde abajo (1-based)
            int blankRow = 0;
            for (int i = 0; i < tiles.length; i++) {
                if (tiles[i] == 0) { blankRow = n - i / n; break; }
            }
            return (inversions + blankRow) % 2 == 1;
        }
    }

    public int getG() { return g; }
    public int[] getTiles() { return Arrays.copyOf(tiles, tiles.length); }
    public int getN() { return n; }
    public PuzzleState getParent() { return parent; }
    public String getMove() { return move; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PuzzleState)) return false;
        return Arrays.equals(tiles, ((PuzzleState) o).tiles);
    }

    @Override
    public int hashCode() { return Arrays.hashCode(tiles); }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tiles.length; i++) {
            sb.append(tiles[i] == 0 ? " _" : String.format("%2d", tiles[i]));
            if ((i + 1) % n == 0) sb.append("\n");
        }
        return sb.toString();
    }
}
