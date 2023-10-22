package com.xbaimiao.lootballoon.core

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import com.xbaimiao.easylib.database.Ormlite
import java.time.LocalDate

/**
 * Database
 *
 * @author xbaimiao
 * @since 2023/10/22 13:34
 */
interface Database {

    fun addTodayCount(playerName: String, balloon: Balloon)

    fun getTodayCount(playerName: String, balloon: Balloon): Int

}

@DatabaseTable(tableName = "loot_balloon")
class LootBalloonTable {

    @DatabaseField(generatedId = true, columnName = "id")
    var id: Long = 0

    @DatabaseField(columnName = "player_name")
    lateinit var playerName: String

    @DatabaseField(columnName = "balloon_name")
    lateinit var balloonName: String

    @DatabaseField(columnName = "day")
    var day: Int = 0

    @DatabaseField(columnName = "today_count")
    var todayCount: Int = 0

}

class DatabaseImpl(ormlite: Ormlite) : Database {

    private val table = ormlite.createDao(LootBalloonTable::class.java)!!

    override fun addTodayCount(playerName: String, balloon: Balloon) {
        val queryBuilder = table.queryBuilder()
        queryBuilder.where().eq("player_name", playerName).and().eq("balloon_name", balloon.name).and().eq("day", LocalDate.now().toEpochDay())
        val result = queryBuilder.query()
        if (result.isEmpty()) {
            val lootBalloonTable = LootBalloonTable()
            lootBalloonTable.playerName = playerName
            lootBalloonTable.balloonName = balloon.name
            lootBalloonTable.day = LocalDate.now().toEpochDay().toInt()
            lootBalloonTable.todayCount = 1
            table.create(lootBalloonTable)
        } else {
            val lootBalloonTable = result[0]
            lootBalloonTable.todayCount++
            table.update(lootBalloonTable)
        }
    }

    override fun getTodayCount(playerName: String, balloon: Balloon): Int {
        val queryBuilder = table.queryBuilder()
        queryBuilder.where().eq("player_name", playerName).and().eq("balloon_name", balloon.name).and().eq("day", LocalDate.now().toEpochDay())
        val result = queryBuilder.query()
        if (result.isEmpty()) {
            return 0
        }
        return result[0].todayCount
    }

}