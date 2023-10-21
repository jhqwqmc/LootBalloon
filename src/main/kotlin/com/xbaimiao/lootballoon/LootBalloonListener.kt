package com.xbaimiao.lootballoon

import com.xbaimiao.easylib.bridge.player.parseToESound
import com.xbaimiao.easylib.util.*
import dev.lone.itemsadder.api.CustomBlock
import dev.lone.itemsadder.api.Events.CustomBlockBreakEvent
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

@EListener
object LootBalloonListener : Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun d(event: MythicMobDeathEvent) {
        if (event.mob.type.internalName == LootBalloonConfig.mobName) {
            val location = BukkitAdapter.adapt(event.mob.entity.location)
            event.drops.clear()
            LootBalloonConfig.mobDeathSound.parseToESound().playSound(location)
            down(location)
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun b(event: CustomBlockBreakEvent) {
        val namespace = CustomBlock.byAlreadyPlaced(event.block)?.namespacedID ?: return
        if (namespace == LootBalloonConfig.iaBlock) {
            drop(event.block.location)
        }
    }

    private fun drop(location: Location) {
        LootBalloonConfig.roll().forEach {
            location.world?.dropItemNaturally(location, it)
        }
    }

    private fun down(location: Location) {
        val entity = MythicBukkit.inst().mobManager.getMythicMob(LootBalloonConfig.mobChestName).getOrNull()
        if (entity == null) {
            warn("未找到生物 ${LootBalloonConfig.mobChestName}")
            return
        }
        val mythicMob = entity.spawn(BukkitAdapter.adapt(location), 1.0)
        val bukkitEntity = BukkitAdapter.adapt(mythicMob.entity)
        bukkitEntity.location.chunk.addPluginChunkTicket(plugin)

        val done = {
            LootBalloonConfig.chestDownSound.parseToESound().playSound(location)
            place(bukkitEntity.location)
            bukkitEntity.location.chunk.removePluginChunkTicket(plugin)
            bukkitEntity.remove()
        }

        submit(period = 2) {
            if (mythicMob.entity.isDead) {
                done.invoke()
                cancel()
                return@submit
            }
            if (mythicMob.entity.isOnGround) {
                done.invoke()
                cancel()
                return@submit
            }
        }

    }

    private fun place(location: Location) {
        val block = CustomBlock.place(LootBalloonConfig.iaBlock, location)
        if (block == null) {
            warn("${LootBalloonConfig.iaBlock} place block failed")
            return
        }
    }

}