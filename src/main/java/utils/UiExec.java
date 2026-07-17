package utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;

/**
 * EDT-safe helpers: background work, coalesced refresh, debounced search.
 */
public final class UiExec {
    private static final ExecutorService BG = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "hotel-bg");
        t.setDaemon(true);
        return t;
    });

    private UiExec() {
    }

    public static <T> void run(ThrowingSupplier<T> background, Consumer<T> onSuccess, Consumer<Exception> onError) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return background.get();
            }

            @Override
            protected void done() {
                try {
                    onSuccess.accept(get());
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    if (onError != null) {
                        onError.accept(cause instanceof Exception ex ? ex : new Exception(cause));
                    }
                }
            }
        }.execute();
    }

    public static void runVoid(ThrowingRunnable background, Runnable onSuccess, Consumer<Exception> onError) {
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                background.run();
                return Boolean.TRUE;
            }

            @Override
            protected void done() {
                try {
                    get();
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    if (onError != null) {
                        onError.accept(cause instanceof Exception ex ? ex : new Exception(cause));
                    }
                }
            }
        }.execute();
    }

    /** Coalesces rapid refresh requests into one trailing call. */
    public static final class Coalescer {
        private final AtomicInteger generation = new AtomicInteger();
        private final int delayMs;
        private Timer timer;
        private Runnable pending;

        public Coalescer(int delayMs) {
            this.delayMs = delayMs;
        }

        public void request(Runnable action) {
            pending = action;
            int gen = generation.incrementAndGet();
            if (timer != null) {
                timer.stop();
            }
            timer = new Timer(delayMs, e -> {
                if (generation.get() == gen && pending != null) {
                    Runnable run = pending;
                    pending = null;
                    run.run();
                }
            });
            timer.setRepeats(false);
            timer.start();
        }

        public void requestNow(Runnable action) {
            if (timer != null) {
                timer.stop();
            }
            pending = null;
            generation.incrementAndGet();
            action.run();
        }
    }

    public static void onEdt(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
