package me.millesant.command;

import cn.nukkit.Player;

public interface CommandHandler
{

    /**
     * Execute a command
     *
     * @param player the player executing the command
     * @param args   command arguments
     * @return true if the command was executed successfully
     */
    boolean execute(
        Player player,
        String[] args
    );

}
