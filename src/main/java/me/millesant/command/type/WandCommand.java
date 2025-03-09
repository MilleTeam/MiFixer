package me.millesant.command.type;

import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;
import me.millesant.MiFixer;
import me.millesant.command.CommandHandler;

public class WandCommand
    implements CommandHandler
{

    private final MiFixer plugin;

    public WandCommand(MiFixer plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(
        Player player,
        String[] args
    )
    {
        if (!player.hasPermission("wf.command.wand") && !player.isOp())
        {
            player.sendMessage(TextFormat.RED + "You don't have permission to use this command");
            return false;
        }

        String playerName = player.getName().toLowerCase();

        if (plugin.hasPlayerSelection(playerName))
        {
            plugin.removePlayerSelection(playerName);
            player.sendMessage(TextFormat.GREEN + "Selection mode disabled");
            return true;
        }

        plugin.createPlayerSelection(playerName);
        player.sendMessage(TextFormat.GREEN + "Now select two blocks");
        return true;
    }

}