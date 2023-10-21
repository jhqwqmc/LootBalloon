package com.xbaimiao.lootballoon.jector

import io.lumine.mythic.api.volatilecode.handlers.VolatileAIHandler
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.core.mobs.ai.PathfinderAdapter
import io.lumine.mythic.core.utils.annotations.MythicAIGoal
import java.lang.reflect.Field
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

internal class MythicInjectorV526(private val logger: Logger) : MythicInjector {

    @Suppress("UNCHECKED_CAST")
    override fun injectGoals(goalClasses: Iterable<Class<*>>) {
        val handler: VolatileAIHandler = MythicBukkit.inst().volatileCodeHandler.aiHandler
        try {
            val aiGoalsField: Field = handler::class.java.getDeclaredField("AI_GOALS")
            aiGoalsField.isAccessible = true
            val aiGoals: MutableMap<String, Class<out PathfinderAdapter?>> =
                aiGoalsField[handler] as MutableMap<String, Class<out PathfinderAdapter?>>
            for (customGoal in goalClasses) {
                val mythicAnnotation = customGoal.getAnnotation(MythicAIGoal::class.java)
                if (mythicAnnotation != null) {
                    aiGoals[mythicAnnotation.name.uppercase(Locale.getDefault())] =
                        customGoal as Class<out PathfinderAdapter?>
                    for (alias in mythicAnnotation.aliases) {
                        aiGoals[alias.uppercase(Locale.getDefault())] = customGoal
                    }
                } else {
                    logger.warning(
                        "Encountered a custom goal class not annotated with @MythicAIGoal: " + customGoal.typeName
                    )
                }
            }
        } catch (e: Throwable) {
            logger.log(Level.WARNING, "Failed to reflect AI goal map", e)
        }
    }

}
