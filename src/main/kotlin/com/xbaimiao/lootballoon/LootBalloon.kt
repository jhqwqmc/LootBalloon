package com.xbaimiao.lootballoon

import com.xbaimiao.easylib.EasyPlugin
import com.xbaimiao.easylib.database.OrmliteSQLite
import com.xbaimiao.easylib.ui.SpigotBasic
import com.xbaimiao.easylib.util.info
import com.xbaimiao.easylib.util.isNotAir
import com.xbaimiao.easylib.util.plugin
import com.xbaimiao.lootballoon.ai.LootBalloonAI
import com.xbaimiao.lootballoon.core.Balloon
import com.xbaimiao.lootballoon.core.DatabaseImpl
import com.xbaimiao.lootballoon.core.LootBalloonRefreshTask
import com.xbaimiao.lootballoon.jector.MythicInjector
import de.tr7zw.changeme.nbtapi.NBTContainer
import de.tr7zw.changeme.nbtapi.NBTItem
import org.bukkit.entity.Player
import java.time.LocalTime

// todo 自动刷新 黑名单世界
@Suppress("unused")
class LootBalloon : EasyPlugin() {

    companion object {
        @JvmStatic
        val inst get() = plugin as LootBalloon
    }

    val balloonList = ArrayList<Balloon>()
    val database by lazy { DatabaseImpl(OrmliteSQLite("database.db")) }

    override fun enable() {
        saveDefaultConfig()
        reload()
        MythicInjector.instance.injectGoals(listOf(LootBalloonAI::class.java))
        LootBalloonRefreshTask.start()
    }

    fun reload() {
        reloadConfig()
        balloonList.clear()
        for (name in config.getKeys(false)) {
            val path = "$name."
            val maxAmount = config.getInt(path + "max-amount")
            val minAmount = config.getInt(path + "min-amount")
            val mobName = config.getString(path + "mob-name")!!
            val mobDeathSound = config.getString(path + "mob-death-sound")!!
            val chestDownSound = config.getString(path + "chest-down-sound")!!
            val mobChestName = config.getString(path + "mob-chest-name")!!
            val mobMoveSpeed = config.getString(path + "mob-move-speed")!!
            val iaBlock = config.getString(path + "ia-block")!!
            val items = config.getStringList(path + "items")
            val worlds = config.getStringList(path + "worlds")
            val time = config.getString(path + "time", "00:00-24:00")!!.split("-").let {
                LocalTime.parse(it[0]) to LocalTime.parse(it[1])
            }
            val maxAmountPerPlayer = config.getInt(path + "max-amount-per-player")
            val probability = config.getDouble(path + "refresh-probability", 0.3)
            balloonList.add(Balloon(
                name, maxAmount, minAmount, mobName, mobDeathSound, chestDownSound, mobChestName, mobMoveSpeed, iaBlock, worlds, time, maxAmountPerPlayer, probability, items
            ).also {
                info("§a加载气球 $name")
            })
        }
    }

    fun edit(player: Player, balloon: Balloon) {
        val basic = SpigotBasic(player, "§c编辑roll物品")

        basic.rows(6)
        basic.handLocked(false)

        balloon.items.withIndex().forEach { (index, item) ->
            if (index > 53) {
                return@forEach
            }
            basic.set(index, NBTItem.convertNBTtoItem(NBTContainer(item))!!)
        }

        basic.onClose { event ->
            balloon.items = ArrayList(event.inventory.contents.filter { it.isNotAir() }.map { NBTItem.convertItemtoNBT(it).toString() })
            config.set("${balloon.name}.items", balloon.items)
            saveConfig()
            player.sendMessage("§c编辑成功")
        }

        basic.open()
    }

}