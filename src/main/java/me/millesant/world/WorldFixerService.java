package me.millesant.world;

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.anvil.RegionLoader;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;
import lombok.SneakyThrows;
import me.millesant.MiFixer;
import me.millesant.block.BlockFixerService;
import me.millesant.block.entity.BlockEntitySpawner;
import me.millesant.math.BoundingBox;
import me.millesant.player.PlayerSelection;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WorldFixerService
{

    private static final int MAX_VOLUME = 1_000_000; // 1 million blocks maximum

    private final MiFixer           plugin;
    private final BlockFixerService blockFixerService;

    // Replace virtual threads with regular threads to avoid concurrency issues
    private final ExecutorService executorService;

    public WorldFixerService(MiFixer plugin)
    {
        this.plugin = plugin;
        this.blockFixerService = plugin.getBlockFixerService();

        // Use a fixed thread pool instead of virtual threads to avoid concurrency issues
        // with Nukkit's chunk system which isn't fully thread-safe
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Fixes a selected area
     *
     * @param selection The player's selection
     * @return true if the area was successfully fixed
     */
    public boolean fixArea(PlayerSelection selection)
    {
        if (!selection.isValid())
        {
            return false;
        }

        BoundingBox box   = selection.getBoundingBox();
        Level       level = box.level();

        if (box.isTooLarge(MAX_VOLUME))
        {
            return false;
        }

        // Using server's main thread instead of submitting to executor service
        // This ensures we're operating on the main thread where Level operations are safe
        plugin.getServer().getScheduler().scheduleTask(plugin, () ->
        {
            int blocksFixed = 0;

            for (
                int x = box.minX();
                x <= box.maxX();
                x++
            )
            {
                for (
                    int z = box.minZ();
                    z <= box.maxZ();
                    z++
                )
                {
                    for (
                        int y = box.minY();
                        y <= box.maxY();
                        y++
                    )
                    {
                        int id = level.getBlockIdAt(x, y, z);
                        if (blockFixerService.fixBlock(level, x, y, z, id))
                        {
                            blocksFixed++;
                        }
                    }
                }
            }

            plugin.getLogger().info("Fixed " + blocksFixed + " blocks in selected area");
        });

        return true;
    }

    /**
     * Fixes an entire world
     *
     * @param worldName The name of the world to fix
     * @param fast      Whether to use fast mode (no delays between region processing)
     */
    public void fixWorld(
        String worldName,
        boolean fast
    )
    {
        Server server = plugin.getServer();
        Level  level  = server.getLevelByName(worldName);

        if (level == null)
        {
            server.loadLevel(worldName);
            level = server.getLevelByName(worldName);
        }

        if (level == null)
        {
            plugin.getLogger().emergency("Level " + worldName + " not found");
            return;
        }

        // Make a copy of the folder name before unloading the level
        final String folder = level.getFolderName();

        // Schedule this on the main server thread
        Level finalLevel1 = level;
        plugin.getServer().getScheduler().scheduleTask(plugin, () ->
        {
            // Unload and reload the level to ensure a clean state
            server.unloadLevel(finalLevel1);
            server.loadLevel(folder);

            Level finalLevel = server.getLevelByName(folder);
            if (finalLevel != null)
            {
                // Process synchronously to avoid concurrency issues
                convertWorld(finalLevel, fast);
            }
            else
            {
                plugin.getLogger().warning("Could not reload level: " + folder);
            }
        });
    }

    /**
     * Converts world by processing all regions
     */
    private void convertWorld(
        Level level,
        boolean fast
    )
    {
        final Pattern pattern = Pattern.compile("-?\\d+");
        final File[] regions = new File(level.getServer().getDataPath() + "worlds/" + level.getFolderName() +
            "/region").listFiles(file -> file.isFile() && file.getName().endsWith(".mca"));

        if (regions == null || regions.length == 0)
        {
            plugin.getLogger().warning("No region files found for world: " + level.getName());
            return;
        }

        final AtomicInteger processedCount     = new AtomicInteger(0);
        final AtomicInteger blocksFixed        = new AtomicInteger(0);
        final AtomicInteger blockEntitiesFixed = new AtomicInteger(0);
        final long          startTime          = System.currentTimeMillis();

        plugin.getLogger().info("Starting to fix world '" + level.getName() + "'");

        // Process regions sequentially to avoid concurrency issues
        for (File region : regions)
        {
            try
            {
                Matcher matcher = pattern.matcher(region.getName());
                int     regionX, regionZ;

                if (matcher.find())
                {
                    regionX = Integer.parseInt(matcher.group());
                }
                else
                {
                    plugin.getLogger().warning("Invalid region filename: " + region.getName());
                    continue;
                }

                if (matcher.find())
                {
                    regionZ = Integer.parseInt(matcher.group());
                }
                else
                {
                    plugin.getLogger().warning("Invalid region filename: " + region.getName());
                    continue;
                }

                final long regionStartTime = System.currentTimeMillis();

                try
                {
                    // Create a new RegionLoader for each region and ensure it's properly closed after use
                    RegionLoader loader = new RegionLoader(level.getProvider(), regionX, regionZ);
                    try
                    {
                        processRegion(level, loader, blocksFixed, blockEntitiesFixed);
                    }
                    finally
                    {
                        loader.close();
                    }
                }
                catch (Exception e)
                {
                    plugin.getLogger().emergency("Error processing region file: " + region.getName());
                    plugin.getLogger().emergency(e.getMessage());
                }

                int    regionsProcessed   = processedCount.incrementAndGet();
                double progressPercentage = NukkitMath.round((double) regionsProcessed / regions.length * 100, 2);
                plugin.getLogger().info("Fixing... " + progressPercentage + "% done");

                if (!fast)
                {
                    // Add a delay proportional to the time taken to process the region
                    long processingTime = System.currentTimeMillis() - regionStartTime;
                    long delayTime      = NukkitMath.floorDouble(processingTime * 0.25);
                    try
                    {
                        Thread.sleep(delayTime);
                    }
                    catch (InterruptedException e)
                    {
                        plugin.getLogger().warning("World fixing process was interrupted");
                        return;
                    }
                }
            }
            catch (Exception e)
            {
                plugin.getLogger().emergency("Unexpected error during region processing");
                plugin.getLogger().emergency(e.getMessage());
            }
        }

        long totalTime = (System.currentTimeMillis() - startTime) / 1000;
        plugin.getLogger().info(TextFormat.GREEN + "World " + level.getName() + " successfully fixed in " + totalTime + "s. (Fixed " + blocksFixed.get() + " blocks and " + blockEntitiesFixed.get() + " block entities)");
    }

    /**
     * Process a single region in a world
     */
    @SneakyThrows
    private void processRegion(
        Level level,
        RegionLoader loader,
        AtomicInteger blocksFixed,
        AtomicInteger blockEntitiesFixed
    )
    {
        for (
            int chunkX = 0;
            chunkX < 32;
            chunkX++
        )
        {
            for (
                int chunkZ = 0;
                chunkZ < 32;
                chunkZ++
            )
            {
                BaseFullChunk chunk = loader.readChunk(chunkX, chunkZ);

                if (chunk == null)
                {
                    continue;
                }

                // Ensure the chunk is properly initialized
                chunk.initChunk();
                boolean chunkChanged = processChunk(level, chunk, blocksFixed, blockEntitiesFixed);

                // Only save if changes were made
                if (chunkChanged)
                {
                    // Write back the modified chunk
                    try
                    {
                        loader.writeChunk(chunk);
                    }
                    catch (Exception e)
                    {
                        plugin.getLogger().emergency("Failed to write back chunk: " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Process a single chunk
     *
     * @return true if the chunk was modified
     */
    private boolean processChunk(
        Level level,
        BaseFullChunk chunk,
        AtomicInteger blocksFixed,
        AtomicInteger blockEntitiesFixed
    )
    {
        boolean chunkChanged = false;

        for (
            int x = 0;
            x < 16;
            x++
        )
        {
            for (
                int z = 0;
                z < 16;
                z++
            )
            {
                for (
                    int y = 0;
                    y < 256;
                    y++
                )
                {
                    int worldX = (chunk.getX() << 4) | (x & 0xf);
                    int worldZ = (chunk.getZ() << 4) | (z & 0xf);
                    int id     = chunk.getBlockId(x, y, z);

                    boolean blockChanged = blockFixerService.fixBlock(level, worldX, y, worldZ, id);
                    if (blockChanged)
                    {
                        chunkChanged = true;
                        blocksFixed.incrementAndGet();
                    }

                    boolean entityChanged = BlockEntitySpawner.checkBlockEntity(id, chunk, new Vector3(x, y, z));
                    if (entityChanged)
                    {
                        chunkChanged = true;
                        blockEntitiesFixed.incrementAndGet();
                    }
                }
            }
        }

        return chunkChanged;
    }

    /**
     * Shuts down the executor service
     */
    public void shutdown()
    {
        executorService.shutdown();
    }

}
