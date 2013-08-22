package io.sunstrike.mantle

import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.{FMLPostInitializationEvent, FMLInitializationEvent, FMLPreInitializationEvent}
import cpw.mods.fml.common.network.NetworkMod
import cpw.mods.fml.common.{FMLCommonHandler, Mod}
import io.sunstrike.mantle.lib.Repo._

/*
 * Mantle
 * io.sunstrike.mantle
 */

/**
 * Mantle Core
 *
 * @author Sunstrike
 */
@Mod(modid = "Mantle", modLanguage = "scala", name = "Mantle", version = "@VERSION@")
@NetworkMod(clientSideRequired = true, serverSideRequired = false)
object Mantle {

    @EventHandler
    def preInit(evt: FMLPreInitializationEvent) {
        logger.setParent(FMLCommonHandler.instance().getFMLLogger)
        logger.info("Mantle preinit completed.")
    }

    @EventHandler
    def init(evt: FMLInitializationEvent) {
        logger.info("Mantle init completed.")
    }

    @EventHandler
    def postInit(evt: FMLPostInitializationEvent) {
        logger.info("Mantle postinit completed.")
    }

}
