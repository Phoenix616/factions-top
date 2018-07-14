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
     * When the task started
     */
    private long startTime;
    /**
     * Time when the lastlog message was send
     */
    private long lastLogMessage;
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
            lastLogMessage = System.currentTimeMillis();
            plugin.getLogger().log(Level.INFO, "Recalculating " + startSize + " chunks...");
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

        if (lastLogMessage + 60 * 1000 < System.currentTimeMillis()) {
            lastLogMessage = System.currentTimeMillis();
            plugin.getLogger().log(Level.INFO, (int) ((startSize - toRecalculate.size()) / (double) startSize * 100 * 100) / 100d + "% done. " + toRecalculate.size() + " chunks remaining...");
        }

        while (isRunning()) {
            if (counter-- <= 0) {
                break;
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
