/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.api

import net.ccbluex.liquidbounce.api.ClientApi.requestMessageOfTheDayEndpoint
import net.ccbluex.liquidbounce.utils.ClientUtils.logger

val messageOfTheDay by lazy {
    try {
        requestMessageOfTheDayEndpoint()
    } catch (e: Exception) {
        logger.error("Unable to receive message of the day", e)
        return@lazy null
    }
}
