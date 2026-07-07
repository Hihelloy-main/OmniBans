package com.hihelloy.work.omnibans.api;

/**
 * Static accessor for the {@link OmniBansApi} instance registered by the running platform.
 *
 * <p>To obtain the API:
 * <pre>{@code
 * if (OmniBansProvider.isLoaded()) {
 *     OmniBansApi api = OmniBansProvider.get();
 * }
 * }</pre>
 *
 * <p>Do not call {@link #register} or {@link #unregister} from third-party code.
 * These are called by OmniBans itself when it enables and disables.
 */
public final class OmniBansProvider {

    private static OmniBansApi instance;

    private OmniBansProvider() {
    }

    /**
     * Returns the registered API instance.
     *
     * @throws IllegalStateException if OmniBans is not loaded or not yet enabled
     */
    public static OmniBansApi get() {
        if (instance == null) {
            throw new IllegalStateException("OmniBans is not loaded. Ensure OmniBans is installed and your plugin depends on it.");
        }
        return instance;
    }

    public static boolean isLoaded() {
        return instance != null;
    }

    /** Called by OmniBans during enable. Not for external use. */
    public static void register(OmniBansApi api) {
        instance = api;
    }

    /** Called by OmniBans during disable. Not for external use. */
    public static void unregister() {
        instance = null;
    }

}
