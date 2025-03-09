package me.millesant.command.type;

import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;
import me.millesant.MiFixer;
import me.millesant.command.CommandHandler;

public class FixLevelCommand
    implements CommandHandler
{

    private final MiFixer plugin;

    public FixLevelCommand(MiFixer plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(
        Player player,
        String[] args
    )
    {
        if (!player.hasPermission("wf.command.fix") && !player.isOp())
        {
            player.sendMessage(TextFormat.RED + "You don't have permission to use this command");
            return false;
        }

        if (args.length < 2)
        {
            player.sendMessage(TextFormat.YELLOW + "Use /wf fixlevel <level name> [fast]");
            return false;
        }

        String  levelName = args[1];
        boolean fast      = args.length > 2 && Boolean.parseBoolean(args[2]);

        // Check if the level exists or can be loaded
        if (plugin.getServer().getLevelByName(levelName) == null)
        {
            if (!plugin.getServer().loadLevel(levelName))
            {
                player.sendMessage(TextFormat.RED + "Level " + TextFormat.YELLOW + levelName +
                    TextFormat.RED + " doesn't exist or couldn't be loaded");
                return false;
            }
        }

        player.sendMessage(TextFormat.YELLOW + "Starting to fix world: " + levelName +
            (fast ? " (fast mode)" : ""));
        player.sendMessage(TextFormat.YELLOW + "This may take some time, please be patient.");

        plugin.fixWorld(levelName, fast);

        return true;
    }

}
