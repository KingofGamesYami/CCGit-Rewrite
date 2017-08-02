package org.kingofgamesyami.ccgit;

import dan200.computercraft.api.ComputerCraftAPI;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created by steve on 6/13/2017.
 */
@Mod(modid = CCGit.MODID, version = CCGit.VERSION, acceptedMinecraftVersions="[1.10.2,1.11.2,]")
public class Main {
    private final LogHandler logger = new LogHandler.FMLLogger();

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        GitRunnable.instance = new GitRunnable( logger );
        ComputerCraftAPI.registerAPIProvider( new Register() );
    }

    @Mod.EventHandler
    public void onServerStart(FMLServerStartedEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
            logger.info( "Attempting to start Git Thread" );
            GitRunnable.instance.start();
        }
    }

    @Mod.EventHandler
    public void onServerStopped(FMLServerStoppedEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
            logger.info( "Stopping Git Thread" );
            GitRunnable.instance.interrupt();
        }
    }
}
