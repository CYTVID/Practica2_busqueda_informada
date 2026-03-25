package mx.ipn.escom.ia.p2.util;

import java.util.List;

/**
 * Extiende SearchResult para incluir opcionalmente la secuencia de estados
 * intermedios, usada para mostrar el paso a paso en consola.
 */
public class DetailedResult extends SearchResult {

    private final List<int[]> statePath;   // secuencia de tableros aplanados
    private final List<String> moveNames;  // nombre de cada movimiento

    public DetailedResult(boolean solved, int nodesExpanded, long timeMs,
                          long memoryKB, String solutionDesc,
                          List<int[]> statePath, List<String> moveNames) {
        super(solved, nodesExpanded, timeMs, memoryKB, solutionDesc);
        this.statePath = statePath;
        this.moveNames = moveNames;
    }

    public List<int[]> getStatePath()  { return statePath; }
    public List<String> getMoveNames() { return moveNames; }
}
