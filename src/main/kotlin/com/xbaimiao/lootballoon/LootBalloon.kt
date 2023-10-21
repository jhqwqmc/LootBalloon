package com.xbaimiao.lootballoon

import com.xbaimiao.easylib.EasyPlugin
import com.xbaimiao.easylib.ui.SpigotBasic
import com.xbaimiao.easylib.util.isNotAir
import com.xbaimiao.easylib.util.plugin
import com.xbaimiao.lootballoon.ai.LootBalloonAI
import com.xbaimiao.lootballoon.jector.MythicInjector
import de.tr7zw.changeme.nbtapi.NBTContainer
import de.tr7zw.changeme.nbtapi.NBTItem
import org.bukkit.entity.Player

@Suppress("unused")
class LootBalloon : EasyPlugin() {

    companion object {
        @JvmStatic
        val inst get() = plugin as LootBalloon
    }

    fun reload() {
        reloadConfig()
    }

    fun edit(player: Player) {
        val basic = SpigotBasic(player, "§c编辑roll物品")

        basic.rows(6)

        LootBalloonConfig.items.withIndex().forEach { (index, item) ->
            if (index > 53) {
                return@forEach
            }
            basic.set(index, NBTItem.convertNBTtoItem(NBTContainer(item))!!)
        }

        basic.onClose { event ->
            LootBalloonConfig.items = ArrayList(event.inventory.contents.filter { it.isNotAir() }.map { NBTItem.convertItemtoNBT(it).toString() })
            config.set("items", LootBalloonConfig.items)
            saveConfig()
            player.sendMessage("§c编辑成功")
        }

        basic.open()
    }

    override fun enable() {
        saveDefaultConfig()
        MythicInjector.instance.injectGoals(listOf(LootBalloonAI::class.java))
    }

}