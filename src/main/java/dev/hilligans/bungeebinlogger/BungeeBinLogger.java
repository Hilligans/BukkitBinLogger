package dev.hilligans.bungeebinlogger;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

public class BungeeBinLogger extends Plugin {

    @Override
    public void onEnable() {
        PluginManager pluginManager = ProxyServer.getInstance().getPluginManager();

        super.onEnable();
    }


    @Override
    public void onDisable() {
        super.onDisable();
    }
}
