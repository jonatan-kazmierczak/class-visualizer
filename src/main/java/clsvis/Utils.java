package clsvis;

/**
 * Various utilities.
 *
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 */
public interface Utils {

    /**
     * Extracts and returns root cause from the given throwable.
     */
    static String rootCauseAsString(Throwable throwable) {
        while (throwable.getCause() != null) {
            throwable = throwable.getCause();
        }
        return throwable.toString();
    }

    /**
     * Returns the type which can be load or null otherwise (primitives, anonymous).
     */
    static Class<?> getClassType(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        // Array - substitute with original class
        while (clazz.isArray()) {
            clazz = clazz.getComponentType();
        }
        // Skip anonymous classes, primitives
        if (clazz.isAnonymousClass() || clazz.isPrimitive()) {
            return null;
        }
        // Return the type
        return clazz;
    }

    /**
     * Returns given color as RRGGBB string.
     */
    static String colorAsRRGGBB(int color) {
        return String.format( "%06X", color );
    }
}
