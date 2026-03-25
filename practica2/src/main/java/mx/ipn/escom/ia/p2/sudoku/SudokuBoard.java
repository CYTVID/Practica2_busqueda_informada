package mx.ipn.escom.ia.p2.sudoku;

import java.util.*;

/**
 * Tablero de Sudoku 9×9.
 * Las celdas con valor 0 se consideran vacías.
 * Provee métodos de validación, generación y clonación.
 */
public class SudokuBoard {

    private final int[][] cells;

    /** Crea un tablero vacío. */
    public SudokuBoard() {
        cells = new int[9][9];
    }

    /** Copia defensiva. */
    public SudokuBoard(int[][] cells) {
        this.cells = new int[9][9];
        for (int i = 0; i < 9; i++)
            this.cells[i] = Arrays.copyOf(cells[i], 9);
    }

    public SudokuBoard copy() {
        return new SudokuBoard(cells);
    }

    public int get(int r, int c) { return cells[r][c]; }
    public void set(int r, int c, int v) { cells[r][c] = v; }

    /** Devuelve true si colocar v en (r,c) no viola ninguna restricción. */
    public boolean isValid(int r, int c, int v) {
        // Fila
        for (int col = 0; col < 9; col++)
            if (cells[r][col] == v) return false;
        // Columna
        for (int row = 0; row < 9; row++)
            if (cells[row][c] == v) return false;
        // Caja 3×3
        int br = (r / 3) * 3, bc = (c / 3) * 3;
        for (int dr = 0; dr < 3; dr++)
            for (int dc = 0; dc < 3; dc++)
                if (cells[br + dr][bc + dc] == v) return false;
        return true;
    }

    /** Cuenta el número de conflictos en el tablero completo. */
    public int countConflicts() {
        int conflicts = 0;
        // Filas
        for (int r = 0; r < 9; r++) {
            int[] seen = new int[10];
            for (int c = 0; c < 9; c++) {
                int v = cells[r][c];
                if (v != 0 && seen[v]++ > 0) conflicts++;
            }
        }
        // Columnas
        for (int c = 0; c < 9; c++) {
            int[] seen = new int[10];
            for (int r = 0; r < 9; r++) {
                int v = cells[r][c];
                if (v != 0 && seen[v]++ > 0) conflicts++;
            }
        }
        // Cajas
        for (int br = 0; br < 3; br++) {
            for (int bc = 0; bc < 3; bc++) {
                int[] seen = new int[10];
                for (int dr = 0; dr < 3; dr++)
                    for (int dc = 0; dc < 3; dc++) {
                        int v = cells[br * 3 + dr][bc * 3 + dc];
                        if (v != 0 && seen[v]++ > 0) conflicts++;
                    }
            }
        }
        return conflicts;
    }

    public boolean isSolved() {
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++)
                if (cells[r][c] == 0) return false;
        return countConflicts() == 0;
    }

    /**
     * Genera un tablero resuelto usando backtracking + aleatorización,
     * luego elimina {@code emptyCells} celdas.
     */
    public static SudokuBoard generate(int emptyCells, Random rng) {
        SudokuBoard board = new SudokuBoard();
        fillBoard(board, 0, rng);
        // Eliminar celdas aleatoriamente
        int removed = 0;
        int attempts = 0;
        while (removed < emptyCells && attempts < 200) {
            int r = rng.nextInt(9);
            int c = rng.nextInt(9);
            if (board.get(r, c) != 0) {
                board.set(r, c, 0);
                removed++;
            }
            attempts++;
        }
        return board;
    }

    /** Backtracking para llenar el tablero completamente. */
    private static boolean fillBoard(SudokuBoard b, int pos, Random rng) {
        if (pos == 81) return true;
        int r = pos / 9, c = pos % 9;
        Integer[] nums = {1,2,3,4,5,6,7,8,9};
        shuffleArray(nums, rng);
        for (int v : nums) {
            if (b.isValid(r, c, v)) {
                b.set(r, c, v);
                if (fillBoard(b, pos + 1, rng)) return true;
                b.set(r, c, 0);
            }
        }
        return false;
    }

    private static void shuffleArray(Integer[] arr, Random rng) {
        for (int i = arr.length - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = arr[i]; arr[i] = arr[j]; arr[j] = tmp;
        }
    }

    /**
     * Renderiza el tablero en ASCII con resaltado de las celdas recién
     * asignadas (marcadas con corchetes) para mostrar el paso a paso.
     *
     * @param highlight array de celdas a destacar como [r, c] pares, puede ser null
     */
    public String toStringHighlighted(Set<String> highlight) {
        StringBuilder sb = new StringBuilder();
        String hBorder = "+-------+-------+-------+";
        for (int r = 0; r < 9; r++) {
            if (r % 3 == 0) sb.append(hBorder).append("\n");
            for (int c = 0; c < 9; c++) {
                if (c % 3 == 0) sb.append("| ");
                int v = cells[r][c];
                String key = r + "," + c;
                if (v == 0) {
                    sb.append(". ");
                } else if (highlight != null && highlight.contains(key)) {
                    sb.append("[").append(v).append("]").append(" ");
                } else {
                    sb.append(v).append(" ");
                }
            }
            sb.append("|\n");
        }
        sb.append(hBorder);
        return sb.toString();
    }

    @Override
    public String toString() {
        return toStringHighlighted(null);
    }
}
