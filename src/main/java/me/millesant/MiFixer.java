package me.millesant;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.utils.LogLevel;
import cn.nukkit.utils.TextFormat;
import lombok.Getter;

import me.millesant.block.BlockFixerService;
import me.millesant.command.CommandHandler;
import me.millesant.command.registry.CommandRegistry;
import me.millesant.command.type.FixCommand;
import me.millesant.command.type.FixLevelCommand;
import me.millesant.command.type.HelpCommand;
import me.millesant.command.type.WandCommand;
import me.millesant.player.PlayerSelection;
import me.millesant.plugin.MiFixerPlugin;
import me.millesant.world.WorldFixerService;

import java.util.Map;
import java.util.Set;

import java.util.concurrent.ConcurrentHashMap;

@Getter
public final class MiFixer
    extends MiFixerPlugin
    implements Listener
{

    private final Map<String, PlayerSelection> playerSelections = new ConcurrentHashMap<>();

    private final Set<String> processedLevels = ConcurrentHashMap.newKeySet();

    private BlockFixerService blockFixerService;

    private WorldFixerService worldFixerService;

    private CommandRegistry commandRegistry;


    @Override
    public void onPluginLoad()
    {

    }

    @Override
    public void onPluginStart()
    {
        this.blockFixerService = new BlockFixerService();

        this.worldFixerService = new WorldFixerService(this);

        this.commandRegistry = new CommandRegistry(this);

        commandRegistry.register("wand", new WandCommand(this));
        commandRegistry.register("fix", new FixCommand(this));
        commandRegistry.register("fixlevel", new FixLevelCommand(this));
        commandRegistry.register("help", new HelpCommand(this));

        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onPluginStop()
    {

    }


    @Override
    public boolean onCommand(
        CommandSender sender,
        Command cmd,
        String label,
        String[] args
    )
    {
        if (!(cmd.getName().equalsIgnoreCase("worldfixer")))
        {
            return false;
        }

        if (args.length == 0)
        {
            sender.sendMessage(TextFormat.RED + "Use /wf help for help");
            return true;
        }

        String         subCommand = args[0].toLowerCase();
        CommandHandler handler    = commandRegistry.getCommand(subCommand);

        if (handler != null)
        {
            return handler.execute(player, args);
        }
        else
        {
            sender.sendMessage(TextFormat.RED + "Unknown command. Use /wf help for help");
            return true;
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block  block  = event.getBlock();

        String playerName = player.getName().toLowerCase();

        if (hasPlayerSelection(playerName)) {
            event.setCancelled();

            PlayerSelection selection = playerSelections.get(playerName);
            switch (selection.getSelectionState())
            {
                case NOT_STARTED,
                     COMPLETED ->
                {
                    var newSelection = selection.withFirstPosition(block.clone());
                    playerSelections.put(playerName, newSelection);
                    player.sendMessage(TextFormat.GREEN + "Selected the first position at " +
                        TextFormat.BLUE + block.x + TextFormat.GREEN + ", " +
                        TextFormat.BLUE + block.y + TextFormat.GREEN + ", " +
                        TextFormat.BLUE + block.z + TextFormat.GREEN);
                }
                case FIRST_POSITION_SET ->
                {
                    var newSelection = selection.withSecondPosition(block.clone());
                    playerSelections.put(playerName, newSelection);
                    player.sendMessage(TextFormat.GREEN + "Selected the second position at " +
                        TextFormat.BLUE + block.x + TextFormat.GREEN + ", " +
                        TextFormat.BLUE + block.y + TextFormat.GREEN + ", " +
                        TextFormat.BLUE + block.z + TextFormat.GREEN);
                }
            }
        }
    }

    public boolean fixSelectedArea(Player player) {
        String playerName = player.getName().toLowerCase();
        if (!isSelectionComplete(playerName)) {
            return false;
        }

        PlayerSelection selection = playerSelections.get(playerName);
        boolean result = worldFixerService.fixArea(selection);

        if (result) {
            playerSelections.remove(playerName);
        }

        return result;
    }

    public void fixWorld(String worldName, boolean fast) {
        try {
            worldFixerService.fixWorld(worldName, fast);
        } catch (Exception e) {
            getLogger().log(LogLevel.EMERGENCY, "Failed to fix world: " + worldName, e);
        }
    }

    public boolean hasPlayerSelection(String playerName) {
        return playerSelections.containsKey(playerName);
    }

    public boolean isSelectionComplete(String playerName) {
        return hasPlayerSelection(playerName) &&
            playerSelections.get(playerName).isComplete();
    }

    public void createPlayerSelection(String playerName) {
        playerSelections.put(playerName, new PlayerSelection());
    }

    public void removePlayerSelection(String playerName) {
        playerSelections.remove(playerName);
    }
    
}
