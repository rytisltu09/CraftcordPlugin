package org.craftcord.craftcordplugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.craftcord.craftcordplugin.bootstrap.PluginBootstrap;

public final class CraftCordPlugin extends JavaPlugin {
    private PluginBootstrap bootstrap;

    @Override
    public void onEnable() {
        this.bootstrap = new PluginBootstrap(this);
        this.bootstrap.start();
    }

    @Override
    public void onDisable() {
        if (bootstrap != null) {
            bootstrap.stop();
        }
    }
}

