package me.millesant.command.type;

import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;
import lombok.Getter;
import me.millesant.MiFixer;
import me.millesant.command.CommandHandler;

@Getter
public class HelpCommand
    implements CommandHandler
{

    private final MiFixer plugin;

    public HelpCommand(MiFixer plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(
        Player player,
        String[] args
    )
    {
        // Using text block (JDK 15+) for better readability when defining help text
        String helpText = """
                          %s> WorldFixer help <%s
                          %s/wf wand %s- select two positions for area fixing
                          %s/wf fix %s- fix all blocks in the selected area
                          %s/wf fixlevel <level name> [fast] %s- fix all blocks in the specified level
                          %s/wf help %s- show this help message
                          """.formatted(
            TextFormat.YELLOW,
            TextFormat.YELLOW,
            TextFormat.GREEN, TextFormat.GRAY,
            TextFormat.GREEN, TextFormat.GRAY,
            TextFormat.GREEN, TextFormat.GRAY,
            TextFormat.GREEN, TextFormat.GRAY
        );

        player.sendMessage(helpText);
        return true;
    }

}
