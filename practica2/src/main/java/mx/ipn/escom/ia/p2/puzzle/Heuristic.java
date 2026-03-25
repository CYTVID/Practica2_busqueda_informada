package mx.ipn.escom.ia.p2.puzzle;

/**
 * Enumeración de las heurísticas admisibles disponibles para A*.
 *
 * <ul>
 *   <li>MISPLACED  – fichas fuera de lugar (número de fichas ≠ meta y ≠ 0).</li>
 *   <li>MANHATTAN  – suma de distancias Manhattan de cada ficha a su posición meta.</li>
 *   <li>CUSTOM     – combinación lineal: Manhattan + 2·conflictos lineales (admisible).</li>
 * </ul>
 */
public enum Heuristic {
    MISPLACED,
    MANHATTAN,
    CUSTOM
}
