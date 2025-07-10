package me.hysong.utils.security;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import javax.swing.JOptionPane;

public final class SingleInstanceEnforcer {
    public enum PromptMode { ADAPTIVE, HEADLESS }
    public enum Scope      { PUBLIC,  PRIVATE  }

    public static final PromptMode ADAPTIVE = PromptMode.ADAPTIVE;
    public static final PromptMode HEADLESS = PromptMode.HEADLESS;
    public static final Scope      PUBLIC   = Scope.PUBLIC;
    public static final Scope      PRIVATE  = Scope.PRIVATE;

    // user-selected prompt behavior
    private static PromptMode promptMode = ADAPTIVE;
    // track all locks by application ID
    private static final Map<String,LockInfo> locks = new HashMap<>();

    // ensure cleanup on JVM exit
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (LockInfo info : locks.values()) {
                try {
                    if (info.lock.isValid()) info.lock.release();
                    if (info.channel.isOpen()) info.channel.close();
                    info.file.delete();
                } catch (IOException ignored) { }
            }
        }));
    }

    // prevent instantiation
    private SingleInstanceEnforcer() { throw new AssertionError(); }

    /**
     * Configure how to notify the user if markRunning(...) finds another instance.
     * Default is ADAPTIVE: show Swing dialog if GUI available, else stderr.
     * HEADLESS always prints to stderr.
     */
    public static void enforcePrompt(PromptMode mode) {
        promptMode = Objects.requireNonNull(mode, "PromptMode cannot be null");
    }

    /**
     * Try to claim "running" status for this application with class canonical name
     * @param scope   PUBLIC: lock file goes in tmp and is discoverable;
     *                PRIVATE: lock file in user-home and effectively hidden.
     * @param appcls  your unique application class
     * @throws InstanceAlreadyRunningException if another JVM holds the lock
     * @throws IOException on I/O failure while creating or locking the file
     */
    public static void markRunning(Scope scope, Class<?> appcls)
            throws InstanceAlreadyRunningException, IOException
    {
        markRunning(scope, appcls.getCanonicalName());
    }

    /**
     * Try to claim “running” status for this application.
     * @param scope  PUBLIC: lock file goes in tmp and is discoverable;
     *               PRIVATE: lock file in user-home and effectively hidden.
     * @param appId  your unique application identifier
     * @throws InstanceAlreadyRunningException if another JVM holds the lock
     * @throws IOException on I/O failure while creating or locking the file
     */
    public static void markRunning(Scope scope, String appId)
            throws InstanceAlreadyRunningException, IOException
    {
        Objects.requireNonNull(scope, "Scope cannot be null");
        Objects.requireNonNull(appId,  "Application ID cannot be null");

        // if called twice for same ID in this JVM, ignore
        if (locks.containsKey(appId)) return;

        File dir = getDirForScope(scope);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Unable to create lock directory: " + dir);
        }

        File lockFile = new File(dir, appId + ".lock");
        FileChannel channel = new RandomAccessFile(lockFile, "rw").getChannel();

        FileLock lock;
        try {
            lock = channel.tryLock();
        } catch (OverlappingFileLockException e) {
            lock = null;
        }

        if (lock == null) {
            String msg = "Another instance of '" + appId + "' is already running.";
            notifyError(msg);
            channel.close();
            throw new InstanceAlreadyRunningException(msg);
        }

        // write PID metadata (optional but helpful)
        channel.truncate(0);
        channel.write(ByteBuffer.wrap(("PID: " + getPid() + "\n").getBytes()));
        channel.force(true);

        locks.put(appId, new LockInfo(lockFile, channel, lock));
    }

    /**
     * List all PUBLIC application IDs currently running (i.e. lock files present).
     */
    public static Set<String> listRunningPublic() {
        File dir = getDirForScope(PUBLIC);
        if (!dir.isDirectory()) return Collections.emptySet();

        String[] files = dir.list((d,n) -> n.endsWith(".lock"));
        if (files == null) return Collections.emptySet();

        Set<String> ids = new HashSet<>();
        for (String f : files) {
            ids.add(f.substring(0, f.length() - 5));  // strip “.lock”
        }
        return ids;
    }

    private static File getDirForScope(Scope s) {
        String base;
        if (s == PUBLIC) {
            base = System.getProperty("java.io.tmpdir")
                    + File.separator + "singleinstance";
        } else {
            base = System.getProperty("user.home")
                    + File.separator + ".singleinstance";
        }
        return new File(base);
    }

    private static void notifyError(String msg) {
        if (promptMode == HEADLESS) {
            System.err.println(msg);
        } else {
            if (!GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(
                        null, msg, "Instance Error", JOptionPane.ERROR_MESSAGE);
            } else {
                System.err.println(msg);
            }
        }
    }

    private static String getPid() {
        String jvm = ManagementFactory.getRuntimeMXBean().getName();
        return jvm.split("@")[0];
    }

    public static class InstanceAlreadyRunningException extends Exception {
        public InstanceAlreadyRunningException(String m) { super(m); }
    }

    // internal holder for cleanup
    private static class LockInfo {
        final File file;
        final FileChannel channel;
        final FileLock lock;
        LockInfo(File f, FileChannel c, FileLock l) {
            this.file    = f;
            this.channel = c;
            this.lock    = l;
        }
    }
}
