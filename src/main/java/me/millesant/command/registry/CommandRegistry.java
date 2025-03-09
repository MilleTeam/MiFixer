package me.millesant.command.registry;

import lombok.Getter;
import me.millesant.MiFixer;

import me.millesant.command.CommandHandler;

import java.util.HashMap;
import java.util.Map;

@Getter
public class CommandRegistry
{

    private final MiFixer                     plugin;
    private final Map<String, CommandHandler> commands = new HashMap<>();

    public CommandRegistry(MiFixer plugin)
    {
        this.plugin = plugin;
    }

    /**
     * Register a command handler
     */
    public void register(
        String name,
        CommandHandler handler
    )
    {
        commands.put(name.toLowerCase(), handler);
    }

    /**
     * Get a command handler by name
     */
    public CommandHandler getCommand(String name)
    {
        return commands.get(name.toLowerCase());
    }

}
