package com.xbaimiao.lootballoon

import com.xbaimiao.easylib.command.command
import com.xbaimiao.easylib.util.CommandBody
import com.xbaimiao.easylib.util.ECommandHeader
import com.xbaimiao.easylib.util.getOrNull
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.MythicBukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@ECommandHeader("lootballoon", permission = "lootballoon.admin")
object LootBalloonCommands {

    @CommandBody
    val reload = command<CommandSender>("reload") {
        description = "重载插件"
        exec {
            LootBalloon.inst.reload()
            sender.sendMessage("§a重载成功")
        }
    }

    @CommandBody
    val edit = command<Player>("edit") {
        description = "编辑气球roll物品"
        exec {
            LootBalloon.inst.edit(sender)
        }
    }

    @CommandBody
    val summon = command<Player>("summon") {
        description = "在当前位置召唤气球"
        exec {
            val mythicMob = MythicBukkit.inst().mobManager.getMythicMob(LootBalloonConfig.mobName).getOrNull()
            if (mythicMob == null) {
                sender.sendMessage("§c召唤失败, 未找到生物 ${LootBalloonConfig.mobName}")
                return@exec
            }
            mythicMob.spawn(BukkitAdapter.adapt(sender.location.also { it.y += 50.0 }), 1.0)
        }
    }

}