package com.xbaimiao.lootballoon.core

import com.xbaimiao.easylib.skedule.launchCoroutine
import com.xbaimiao.easylib.util.onlinePlayers
import com.xbaimiao.lootballoon.LootBalloon

object LootBalloonRefreshTask {

    fun start() {
        launchCoroutine {
            while (true) {
                for (onlinePlayer in onlinePlayers()) {
                    a@ for (balloon in LootBalloon.inst.balloonList) {
                        if (balloon.refresh(onlinePlayer)) {
                            break@a
                        } else {
                            continue@a
                        }
                    }
                }
                waitFor(60)
            }
        }
    }

}