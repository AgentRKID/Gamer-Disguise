package games.scorpio.disguise.util;

import games.scorpio.disguise.GamerDisguise;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.Bukkit;

public class Tasks {

    /**
     * Runs the {@param runnable} safely making sure we're actually on an async/not main thread
     * before attempting to add to the runnable queue for next tick
     */
    public static void safeRunSync(Runnable runnable) {
        if (isSync()) {
            runnable.run();
            return;
        }
        run(runnable);
    }

    public static void run(Runnable runnable) {
        Bukkit.getScheduler().runTask(GamerDisguise.getInstance(), runnable);
    }

    public static void runLater(Runnable runnable, long delay) {
        Bukkit.getScheduler().runTaskLater(GamerDisguise.getInstance(), runnable, delay);
    }

    public static void runTimer(Runnable runnable, long interval) {
        Bukkit.getScheduler().runTaskTimer(GamerDisguise.getInstance(), runnable, 0, interval);
    }

    /**
     * Runs the {@param runnable} safely making sure we're actually on the main thread
     * before attempting to add to the async queue, else it runs it in the current runnable
     * async queue/thread
     */
    public static void safeRunAsync(Runnable runnable) {
        if (isAsync()) {
            runnable.run();
            return;
        }
        runAsync(runnable);
    }

    public static void runAsync(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(GamerDisguise.getInstance(), runnable);
    }

    public static void runAsyncLater(Runnable runnable, long delay) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(GamerDisguise.getInstance(), runnable, delay);
    }

    public static void runAsyncTimer(Runnable runnable, long interval) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(GamerDisguise.getInstance(), runnable, 0, interval);
    }

    public static boolean isAsync() {
        return Thread.currentThread() != MinecraftServer.getServer().primaryThread;
    }

    public static boolean isSync() {
        return Bukkit.isPrimaryThread();
    }

}
