/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot.isBot
import net.ccbluex.liquidbounce.features.module.modules.misc.Teams
import net.ccbluex.liquidbounce.utils.extensions.isAnimal
import net.ccbluex.liquidbounce.utils.extensions.isClientFriend
import net.ccbluex.liquidbounce.utils.extensions.isMob
import net.ccbluex.liquidbounce.value._boolean
import net.ccbluex.liquidbounce.value.floatValue
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer

object HitBox : Module("HitBox", Category.COMBAT, hideModule = false) {

    private val targetPlayers by _boolean("TargetPlayers", true)
    private val playerSize by floatValue("PlayerSize", 0.4F, 0F..1F) { targetPlayers }
    private val friendSize by floatValue("FriendSize", 0.4F, 0F..1F) { targetPlayers }
    private val teamMateSize by floatValue("TeamMateSize", 0.4F, 0F..1F) { targetPlayers }
    private val botSize by floatValue("BotSize", 0.4F, 0F..1F) { targetPlayers }

    private val targetMobs by _boolean("TargetMobs", false)
    private val mobSize by floatValue("MobSize", 0.4F, 0F..1F) { targetMobs }

    private val targetAnimals by _boolean("TargetAnimals", false)
    private val animalSize by floatValue("AnimalSize", 0.4F, 0F..1F) { targetAnimals }

    fun determineSize(entity: Entity): Float {
        return when (entity) {
            is EntityPlayer -> {
                if (entity.isSpectator || !targetPlayers) {
                    return 0F
                }

                if (isBot(entity)) {
                    return botSize
                } else if (entity.isClientFriend() && !handleEvents()) {
                    return friendSize
                } else if (handleEvents() && Teams.isInYourTeam(entity)) {
                    return teamMateSize
                }

                playerSize
            }

            else -> {
                if (entity.isMob() && targetMobs) {
                    return mobSize
                } else if (entity.isAnimal() && targetAnimals) {
                    return animalSize
                }

                0F
            }
        }
    }
}