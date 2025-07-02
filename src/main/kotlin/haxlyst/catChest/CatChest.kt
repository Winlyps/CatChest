package haxlyst.catChest

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Cat
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.Location

class CatChest : JavaPlugin(), Listener {

    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
        logger.info("CatChest enabled: Cats will no longer block chests!")
    }

    override fun onDisable() {
        logger.info("CatChest disabled.")
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return

        val block = event.clickedBlock ?: return
        val type = block.type

        if (type != Material.CHEST && type != Material.TRAPPED_CHEST) return

        // Get the block above the chest
        val aboveBlock = block.getRelative(BlockFace.UP)

        // Find all cats sitting on top of the chest
        val cats = aboveBlock.location.world?.getNearbyEntities(aboveBlock.location, 0.5, 0.5, 0.5)
            ?.filterIsInstance<Cat>()
            ?.filter { it.isSitting } ?: return

        if (cats.isEmpty()) return

        // Temporarily move cats off the chest
        cats.forEach { cat ->
            cat.teleport(cat.location.add(1.0, 0.0, 0.0)) // Move cat 1 block to the side
        }

        // Open the chest for the player
        Bukkit.getScheduler().runTask(this, Runnable {
            event.player.openInventory((block.state as org.bukkit.block.Chest).blockInventory)
            // Move cats back after a short delay
            Bukkit.getScheduler().runTaskLater(this, Runnable {
                cats.forEach { cat ->
                    cat.teleport(aboveBlock.location.add(0.5, 0.0, 0.5))
                }
            }, 20L) // 1 second later
        })

        event.isCancelled = true // Prevent default interaction (since we handle chest opening)
    }
}
