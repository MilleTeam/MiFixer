package me.millesant.command.type;

import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;
import me.millesant.MiFixer;
import me.millesant.command.CommandHandler;

public class FixCommand
    implements CommandHandler
{

    private final MiFixer plugin;

    public FixCommand(MiFixer plugin)
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

        if (!plugin.isSelectionComplete(player.getName().toLowerCase()))
        {
            player.sendMessage(TextFormat.RED + "You must select both positions first!");
            return false;
        }

        boolean result = plugin.fixSelectedArea(player);

        if (result)
        {
            player.sendMessage(TextFormat.GREEN + "Selected area has been successfully fixed!");
            return true;
        }
        else
        {
            player.sendMessage(TextFormat.RED + "Failed to fix the selected area. Make sure both positions are in " +
                "the" + " same level!");
            return false;
        }
    }

}
