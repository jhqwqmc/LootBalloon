package com.xbaimiao.lootballoon.core

import com.xbaimiao.easylib.bridge.player.parseToESound
import com.xbaimiao.easylib.skedule.launchCoroutine
import com.xbaimiao.easylib.util.getOrNull
import com.xbaimiao.easylib.util.plugin
import com.xbaimiao.easylib.util.submit
import com.xbaimiao.easylib.util.warn
import com.xbaimiao.lootballoon.LootBalloon
import de.tr7zw.changeme.nbtapi.NBTContainer
import de.tr7zw.changeme.nbtapi.NBTItem
import dev.lone.itemsadder.api.CustomBlock
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.MythicBukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import java.time.LocalTime
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Balloon
 *
 * @author xbaimiao
 * @since 2023/10/22 08:00
 */
class Balloon(
    val name: String,
    private val maxAmount: Int,
    private val minAmount: Int,
    val mobName: String,
    private val mobDeathSound: String,
    private val chestDownSound: String,
    private val mobChestName: String,
    private val mobMoveSpeed: String,
    val iaBlock: String,
    private val worlds: List<String>,
    private val time: Pair<LocalTime, LocalTime>,
    private val maxAmountPerPlayer: Int,
    private val probability: Double,
    var items: List<String>
) {

    suspend fun refresh(player: Player): Boolean = suspendCoroutine {
        launchCoroutine {
            val max = async {
                LootBalloon.inst.database.getTodayCount(player.name, this@Balloon) >= maxAmountPerPlayer
            }
            if (max) {
                it.resume(false)
                return@launchCoroutine
            }
            if (player.world.name in worlds) {
                it.resume(false)
                return@launchCoroutine
            }
            if (Math.random() > probability) {
                it.resume(false)
                return@launchCoroutine
            }
            val now = LocalTime.now()
            if (now !in time.first..time.second) {
                it.resume(false)
                return@launchCoroutine
            }
            summon(player.location.clone().also { it.y += 30 })
            async {
                LootBalloon.inst.database.addTodayCount(player.name, this@Balloon)
            }
            it.resume(true)
        }
    }

    fun summon(location: Location) {
        val mythicMob = MythicBukkit.inst().mobManager.getMythicMob(mobName).getOrNull()
        if (mythicMob == null) {
            warn("§c召唤失败, 未找到生物 $mobName")
            return
        }
        mythicMob.spawn(BukkitAdapter.adapt(location), 1.0).also {
            it.entity.bukkitEntity.setMetadata("LootBalloonMoveSpeed", FixedMetadataValue(plugin, mobMoveSpeed))
        }
    }

    fun death(location: Location) {
        mobDeathSound.parseToESound().playSound(location)
        down(location)
    }

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


    private fun down(location: Location) {
        val entity = MythicBukkit.inst().mobManager.getMythicMob(mobChestName).getOrNull()
        if (entity == null) {
            warn("未找到生物 $mobChestName")
            return
        }
        val mythicMob = entity.spawn(BukkitAdapter.adapt(location), 1.0)
        val bukkitEntity = BukkitAdapter.adapt(mythicMob.entity)
        bukkitEntity.location.chunk.addPluginChunkTicket(plugin)

        val done = {
            chestDownSound.parseToESound().playSound(location)
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
        val block = CustomBlock.place(iaBlock, location)
        if (block == null) {
            warn("$iaBlock place block failed")
            return
        }
    }

    override fun toString(): String {
        return "Balloon(" +
                "name='$name', " +
                "maxAmount=$maxAmount, " +
                "minAmount=$minAmount, " +
                "mobName='$mobName', " +
                "mobMoveSpeed='$mobMoveSpeed', " +
                "iaBlock='$iaBlock', " +
                "worlds=$worlds, " +
                "time=$time, " +
                "maxAmountPerPlayer=$maxAmountPerPlayer" +
                ")"
    }

}