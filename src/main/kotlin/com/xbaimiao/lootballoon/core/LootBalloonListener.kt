package com.xbaimiao.lootballoon.core

import com.xbaimiao.easylib.util.EListener
import com.xbaimiao.lootballoon.LootBalloon
import dev.lone.itemsadder.api.CustomBlock
import dev.lone.itemsadder.api.Events.CustomBlockBreakEvent
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

@EListener
object LootBalloonListener : Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun d(event: MythicMobDeathEvent) {
        LootBalloon.inst.balloonList.filter { it.mobName == event.mob.type.internalName }.forEach {
            event.drops.clear()
            it.death(BukkitAdapter.adapt(event.mob.entity.location))
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun b(event: CustomBlockBreakEvent) {
        val block = CustomBlock.byAlreadyPlaced(event.block) ?: return
        val namespace = block.namespacedID
        val balloon = LootBalloon.inst.balloonList.firstOrNull { it.iaBlock == namespace } ?: return
        val location = event.block.location
        balloon.roll().forEach {
            location.world?.dropItemNaturally(location, it)
        }
        event.isCancelled = true
        block.remove()
    }

}