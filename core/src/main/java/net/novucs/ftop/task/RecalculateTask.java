package net.novucs.ftop.task;

import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.PluginService;
import net.novucs.ftop.RecalculateReason;
import net.novucs.ftop.entity.ChunkPos;
import org.bukkit.Chunk;

import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class RecalculateTask implements PluginService, Runnable {

    private final FactionsTopPlugin plugin;
    /**
     * The chunks which we need to calculate
     */
    private final Stack<ChunkPos> toRecalculate = new Stack<>();
    /**
     * How often in seconds it should print an information message.
     * Automatically calculated from the size of chunks to not spam the console too much.
     */
    private int logInterval;
    /**
     * When the task started
     */
    private long startTime;
    /**
     * How much chunks we had at the start
     */
    private int startSize;
    /**
     * The ID of this task
     */
    private int taskId;

    public RecalculateTask(FactionsTopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        if (!isRunning()) {
            plugin.getServer().broadcastMessage(plugin.getSettings().getRecalculationStartMessage());
            toRecalculate.addAll(plugin.getFactionsHook().getClaims());
            startSize = toRecalculate.size();
            startTime = System.currentTimeMillis();
            int expectedTime = startSize / plugin.getSettings().getRecalculateChunksPerTick() / 20 / 60;
            if (expectedTime > 10) {
                logInterval = 60 * 5;
            } else if (expectedTime > 5) {
                logInterval = 60;
            } else {
                logInterval = 30;
            }
            plugin.getLogger().log(Level.INFO, "Recalculating " + startSize + " chunks... Expected time: " + (expectedTime <= 1 ? "1 minute" : expectedTime + " minutes"));
            taskId = plugin.getServer().getScheduler().runTaskTimer(plugin, this, 1, 1).getTaskId();
        } else {
            throw new IllegalStateException("Recalculation task is already running");
        }
    }

    @Override
    public void terminate() {
        if (isRunning()) {
            toRecalculate.clear();
            plugin.getServer().getScheduler().cancelTask(taskId);
            plugin.getServer().broadcastMessage(plugin.getSettings().getRecalculationStopMessage());
        } else {
            throw new IllegalStateException("No recalculation task was running");
        }
    }

    public boolean isRunning() {
        return !toRecalculate.isEmpty();
    }

    @Override
    public void run() {
        int counter = plugin.getSettings().getRecalculateChunksPerTick();

        while (isRunning()) {
            if (counter-- <= 0) {
                break;
            }

            if (toRecalculate.size() % (plugin.getSettings().getRecalculateChunksPerTick() * 20 * logInterval) == 0) {
                plugin.getLogger().log(Level.INFO, (toRecalculate.size() / (double) startSize) + "% done. " + toRecalculate.size() + " chunks remaining...");
            }

            ChunkPos pos = toRecalculate.pop();
            Chunk chunk = pos.getChunk(plugin.getServer());
            if (chunk != null && chunk.load(false)) {
                plugin.getWorthManager().recalculate(chunk, RecalculateReason.COMMAND);
            }
        }

        if (!isRunning()) {
            plugin.getServer().getScheduler().cancelTask(taskId);
            plugin.getLogger().log(Level.INFO, "Finished recalculating chunks in " + TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - startTime) + " minutes");
            plugin.getLogger().log(Level.INFO, "Updating all " + plugin.getSettings().getFactionTypeName() + "s now...");
            plugin.getWorthManager().updateAllFactions();
            plugin.getServer().broadcastMessage(plugin.getSettings().getRecalculationFinishMessage());
        }
    }
}
