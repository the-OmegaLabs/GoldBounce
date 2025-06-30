/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value._boolean
import net.ccbluex.liquidbounce.value.floatValue

object ItemPhysics : Module("ItemPhysics", Category.RENDER, hideModule = false) {

    val realistic by _boolean("Realistic", false)
    val weight by floatValue("Weight", 0.5F, 0.1F..3F)
    val rotationSpeed by floatValue("RotationSpeed", 1.0F, 0.01F..3F)

}