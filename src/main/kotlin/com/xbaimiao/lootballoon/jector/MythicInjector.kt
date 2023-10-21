package com.xbaimiao.lootballoon.jector

import com.xbaimiao.easylib.util.plugin

interface MythicInjector {

    fun injectGoals(goalClasses: Iterable<Class<*>>)

    companion object {
        val instance: MythicInjector by lazy { MythicInjectorV526(plugin.logger) }
    }

}