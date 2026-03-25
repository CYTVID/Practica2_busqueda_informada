package mx.ipn.escom.ia.p2.util;

/**
 * Utilidad para estimar el consumo de memoria del heap de la JVM.
 * Llama a gc() antes de tomar la medición base para minimizar el ruido.
 */
public class MemoryUtil {

    private long baseBytes;

    /** Registra la memoria usada en este momento como línea base. */
    public void start() {
        Runtime rt = Runtime.getRuntime();
        System.gc();
        baseBytes = rt.totalMemory() - rt.freeMemory();
    }

    /** Retorna la memoria adicional (sobre la base) en kilobytes. */
    public long elapsedKB() {
        Runtime rt = Runtime.getRuntime();
        long current = rt.totalMemory() - rt.freeMemory();
        long delta = current - baseBytes;
        return delta > 0 ? delta / 1024 : 0;
    }
}
