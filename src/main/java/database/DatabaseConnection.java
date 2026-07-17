package database;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Small JDBC connection pool. {@code close()} returns connections to the pool
 * instead of tearing down the TCP session — major win for SwingWorker DAO calls.
 */
public final class DatabaseConnection {
    private static final int POOL_SIZE = 6;
    private static final BlockingQueue<Connection> POOL = new LinkedBlockingQueue<>(POOL_SIZE);
    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

    private DatabaseConnection() {
    }

    public static void warmup() throws SQLException {
        startPool();
    }

    private static void startPool() throws SQLException {
        if (!STARTED.compareAndSet(false, true)) {
            return;
        }
        for (int i = 0; i < POOL_SIZE; i++) {
            POOL.offer(createPhysical());
        }
    }

    private static Connection createPhysical() throws SQLException {
        Connection conn = DriverManager.getConnection(
                DatabaseConfig.getUrl(),
                DatabaseConfig.getUser(),
                DatabaseConfig.getPassword()
        );
        conn.setAutoCommit(true);
        return conn;
    }

    public static Connection getConnection() throws SQLException {
        startPool();
        Connection raw = null;
        try {
            raw = POOL.poll(80, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (raw == null || !isHealthy(raw)) {
            if (raw != null) {
                silentlyClose(raw);
            }
            raw = createPhysical();
        }
        return wrap(raw);
    }

    private static boolean isHealthy(Connection conn) {
        try {
            return conn != null && !conn.isClosed() && conn.isValid(1);
        } catch (SQLException e) {
            return false;
        }
    }

    private static void silentlyClose(Connection conn) {
        try {
            conn.close();
        } catch (SQLException ignored) {
            // ignore
        }
    }

    private static Connection wrap(Connection physical) {
        InvocationHandler handler = new InvocationHandler() {
            private boolean closed;

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String name = method.getName();
                if ("close".equals(name)) {
                    if (!closed) {
                        closed = true;
                        recycle(physical);
                    }
                    return null;
                }
                if ("isClosed".equals(name)) {
                    return closed || physical.isClosed();
                }
                if (closed) {
                    throw new SQLException("Connection already returned to pool");
                }
                try {
                    return method.invoke(physical, args);
                } catch (InvocationTargetException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof Exception) {
                        throw cause;
                    }
                    throw e;
                }
            }
        };
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                handler
        );
    }

    private static void recycle(Connection physical) {
        try {
            if (!physical.getAutoCommit()) {
                physical.rollback();
                physical.setAutoCommit(true);
            }
            if (!POOL.offer(physical)) {
                silentlyClose(physical);
            }
        } catch (SQLException e) {
            silentlyClose(physical);
        }
    }

    public static void shutdown() {
        Connection c;
        while ((c = POOL.poll()) != null) {
            silentlyClose(c);
        }
        STARTED.set(false);
    }
}
