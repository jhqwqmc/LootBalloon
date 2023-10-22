package com.xbaimiao.lootballoon.ai

import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.core.mobs.ai.Pathfinder
import io.lumine.mythic.core.mobs.ai.PathfindingGoal
import io.lumine.mythic.core.utils.annotations.MythicAIGoal
import org.bukkit.Location
import org.bukkit.util.Vector
import kotlin.math.atan2

/**
 * LootBalloonAI
 *
 * @author xbaimiao
 * @since 2023/10/21 21:16
 */
@MythicAIGoal(
    name = "lootballoon",
    aliases = ["lt"],
    version = "4.8",
    description = "Floating in a random straight line, committing suicide when encountering obstacles"
)
class LootBalloonAI(
    entity: AbstractEntity,
    line: String,
    mythicLineConfig: MythicLineConfig
) : Pathfinder(entity, line, mythicLineConfig), PathfindingGoal {

    private val posA: Location = BukkitAdapter.adapt(activeMob.spawnLocation)

    // posB 为离 posA 200格的位置 有可能是在 东南西北
    private val posB = posA.clone().add((Math.random() * 1000 - 500), 0.0, (Math.random() * 1000 - 500))

    override fun shouldStart(): Boolean {
        return !activeMob.isDead
    }

    override fun start() {

    }

    override fun tick() {
        val location = BukkitAdapter.adapt(activeMob.location).clone()

        val myX = location.x
        val myY = location.y
        val myZ = location.z
        val now = Vector(myX, myY, myZ)

        // 距离
        val distanceSquared = posB.toVector().distanceSquared(now)

        val bukkitEntity = activeMob.entity.bukkitEntity
        val moveSpeed = if (bukkitEntity.hasMetadata("LootBalloonMoveSpeed")) {
            bukkitEntity.getMetadata("LootBalloonMoveSpeed")[0].asFloat()
        } else {
            0.2f
        }

        if (distanceSquared < moveSpeed) {
            // 到达终点
            activeMob.remove()
            return
        }
        val move = posB.toVector().subtract(now).normalize().multiply(moveSpeed)

        val d = now.clone().subtract(posB.toVector()).normalize()

        now.add(move)

        val moveLocation = now.toLocation(BukkitAdapter.adapt(activeMob.spawnLocation.world!!))
        moveLocation.yaw = (atan2(d.z, d.x) * (180.0 / Math.PI) - 90.0).toFloat()

        if (!moveLocation.block.isPassable) {
            // 撞到山
            activeMob.remove()
            return
        }

        activeMob.entity.teleport(BukkitAdapter.adapt(moveLocation))
    }

    override fun shouldEnd(): Boolean {
        return activeMob.isDead
    }

    override fun end() {

    }

}