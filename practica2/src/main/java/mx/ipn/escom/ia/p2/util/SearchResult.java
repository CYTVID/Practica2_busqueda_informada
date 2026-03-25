package mx.ipn.escom.ia.p2.util;

/**
 * Encapsula los resultados de una búsqueda:
 * solución encontrada, nodos expandidos, tiempo (ms) y memoria pico (KB).
 */
public class SearchResult {
    private final boolean solved;
    private final int nodesExpanded;
    private final long timeMs;
    private final long memoryKB;
    private final String solutionDesc;

    public SearchResult(boolean solved, int nodesExpanded,
                        long timeMs, long memoryKB, String solutionDesc) {
        this.solved = solved;
        this.nodesExpanded = nodesExpanded;
        this.timeMs = timeMs;
        this.memoryKB = memoryKB;
        this.solutionDesc = solutionDesc;
    }

    public boolean isSolved()        { return solved; }
    public int    getNodesExpanded() { return nodesExpanded; }
    public long   getTimeMs()        { return timeMs; }
    public long   getMemoryKB()      { return memoryKB; }
    public String getSolutionDesc()  { return solutionDesc; }

    @Override
    public String toString() {
        return String.format("Resuelto: %s | Nodos: %d | Tiempo: %d ms | Memoria: %d KB%n  %s",
                solved, nodesExpanded, timeMs, memoryKB, solutionDesc);
    }
}
