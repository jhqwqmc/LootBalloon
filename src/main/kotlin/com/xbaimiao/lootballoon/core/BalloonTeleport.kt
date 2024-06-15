package com.xbaimiao.lootballoon.core

import com.google.common.cache.CacheBuilder
import org.bukkit.Location
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author xbaimiao
 * @date 2024/6/15
 * @email owner@xbaimiao.com
 */
object BalloonTeleport {


    data class Data(val balloon: Balloon, val location: Location)

    private val cache = CacheBuilder.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build<String, Data>()

    /**
     * 缓存一个气球位置
     * @return token
     */
    fun cache(balloon: Balloon, location: Location): String {
        val data = Data(balloon, location)
        val token = UUID.randomUUID().toString().replace("-", "").substring(0, 8)
        cache.put(token, data)
        return token
    }

    fun getLocation(token: String): Location? {
        val data = cache.getIfPresent(token) ?: return null
        val origin = data.location.clone()

        val randomX = (data.balloon.teleportOffset * Math.random()) * (if (Random().nextBoolean()) 1 else -1)
        val randomZ = (data.balloon.teleportOffset * Math.random()) * (if (Random().nextBoolean()) 1 else -1)
        val newLocation = origin.apply {
            x += randomX
            z += randomZ
        }
        newLocation.y = newLocation.world!!.getHighestBlockYAt(newLocation).toDouble()
        return newLocation
    }

}
