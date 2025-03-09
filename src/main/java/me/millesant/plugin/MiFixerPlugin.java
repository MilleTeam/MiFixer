package me.millesant.plugin;

import cn.nukkit.plugin.PluginBase;

public abstract class MiFixerPlugin
    extends PluginBase
{

    @Override
    public final void onLoad()
    {
        this.onPluginLoad();
    }

    public abstract void onPluginLoad();

    @Override
    public final void onEnable()
    {
        this.onPluginStart();
    }

    public abstract void onPluginStart();

    @Override
    public final void onDisable()
    {
        this.onPluginStop();
    }

    public abstract void onPluginStop();

}
