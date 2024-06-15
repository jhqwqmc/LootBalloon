package com.xbaimiao.lootballoon.core

import com.xbaimiao.easylib.chat.Lang.sendLang
import com.xbaimiao.easylib.command.ArgNode
import com.xbaimiao.easylib.command.command
import com.xbaimiao.easylib.util.CommandBody
import com.xbaimiao.easylib.util.ECommandHeader
import com.xbaimiao.lootballoon.LootBalloon
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@ECommandHeader("lootballoon")
object LootBalloonCommands {

    private val balloonArgNode = ArgNode("气球名称", exec = { token ->
        LootBalloon.inst.balloonList.map { it.name }.filter { it.startsWith(token) }
    }, parse = { token ->
        LootBalloon.inst.balloonList.find { it.name == token }
    })

    @CommandBody
    val reload = command<CommandSender>("reload") {
        description = "重载插件"
        permission = "lootballoon.admin"
        exec {
            LootBalloon.inst.reload()
            sender.sendMessage("§a重载成功")
        }
    }

    @CommandBody
    val edit = command<Player>("edit") {
        description = "编辑气球roll物品"
        permission = "lootballoon.admin"
        val balloonArg = arg(balloonArgNode)
        exec {
            val balloon = balloonArg.value()
            if (balloon == null) {
                sender.sendMessage("§c编辑失败, 未找到气球 ${balloonArg.argString()}")
                return@exec
            }
            LootBalloon.inst.edit(sender, balloon)
        }
    }

    @CommandBody
    val summon = command<Player>("summon") {
        description = "在当前位置召唤气球"
        permission = "lootballoon.admin"
        val balloonArg = arg(balloonArgNode)
        exec {
            val balloon = balloonArg.value()
            if (balloon == null) {
                sender.sendMessage("§c召唤失败, 未找到气球 ${balloonArg.argString()}")
                return@exec
            }
            balloon.summon(sender.location.also { it.y += balloon.height })
        }
    }

    @CommandBody
    val api = command<Player>("api") {
        description = "点击聊天信息的API接口"
        val apiArg = arg("API接口")
        exec {
            val api = apiArg.value()
            if (api.startsWith("teleport|")) {
                val token = api.substringAfter("teleport|")
                val location = BalloonTeleport.getLocation(token)
                if (location == null) {
                    sender.sendLang("error-api-token")
                    return@exec
                }
                sender.teleport(location)
                sender.sendLang("teleport-success")
            } else {
                sender.sendLang("error-api")
            }
        }
    }

}
