package com.xbaimiao.lootballoon

import com.xbaimiao.easylib.util.ConfigNode
import com.xbaimiao.easylib.util.EConfig
import de.tr7zw.changeme.nbtapi.NBTContainer
import de.tr7zw.changeme.nbtapi.NBTItem
import org.bukkit.inventory.ItemStack

@EConfig("config.yml")
object LootBalloonConfig {

    @ConfigNode("max-amount")
    var maxAmount = 5

    @ConfigNode("min-amount")
    var minAmount = 1

    @ConfigNode("mob-name")
    var mobName = "LootBalloon"

    @ConfigNode("mob-death-sound")
    var mobDeathSound = "ENTITY_PLAYER_LEVELUP"

    @ConfigNode("chest-down-sound")
    var chestDownSound = "ENTITY_PLAYER_LEVELUP"

    @ConfigNode("chest-mob-name")
    var mobChestName = "LootBalloon-Chest"

    @ConfigNode("mob-move-speed")
    var mobMoveSpeed = 0.5

    @ConfigNode("ia-block")
    var iaBlock = "blocks_expansion:balloon_crate"

    @ConfigNode("items")
    var items = ArrayList<String>()

    fun roll(): List<ItemStack> {
        if (items.isEmpty()) {
            return emptyList()
        }
        val result = HashSet<String>()
        val amount = (minAmount..maxAmount).random()
        repeat(amount) {
            result.add(items.random())
        }
        return result.map { NBTItem.convertNBTtoItem(NBTContainer(it))!! }
    }

}