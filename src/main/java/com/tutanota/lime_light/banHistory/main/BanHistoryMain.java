package com.tutanota.lime_light.banHistory.main;

import com.tutanota.lime_light.banHistory.commands.CommandBanHistory;
import com.tutanota.lime_light.banHistory.proxy.CommonProxy;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Reference.MODID, name = Reference.NAME, version = Reference.VERSION)
public class BanHistoryMain
{
    @SidedProxy(serverSide = Reference.SERVER_PROXY_CLASS, clientSide = Reference.CLIENT_PROXY_CLASS)
    public static CommonProxy proxy;

    @Mod.Instance(Reference.MODID)
    private static BanHistoryMain instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        try{
            instance = this;
            MinecraftForge.EVENT_BUS.register(this);
            ClientCommandHandler.instance.registerCommand(new CommandBanHistory());
        }catch(Exception e)
        {
            System.out.println("An exception occurred in preInit(). Stacktrace below:");
            e.printStackTrace();
        }
    }
    @Mod.EventHandler
    public void init(FMLInitializationEvent event){}
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event){}
}
