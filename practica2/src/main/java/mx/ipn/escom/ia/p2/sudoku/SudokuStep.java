package mx.ipn.escom.ia.p2.sudoku;

/**
 * Representa un paso en la resolución del Sudoku:
 * la celda asignada, el valor colocado y el tablero resultante.
 */
public class SudokuStep {
    public final int row;
    public final int col;
    public final int value;
    public final SudokuBoard board;

    public SudokuStep(int row, int col, int value, SudokuBoard board) {
        this.row   = row;
        this.col   = col;
        this.value = value;
        this.board = board;
    }
}
