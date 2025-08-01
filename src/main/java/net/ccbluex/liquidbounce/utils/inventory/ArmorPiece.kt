/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.utils.inventory

import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack

class ArmorPiece(val itemStack: ItemStack, val slot: Int) {
    val armorType = (itemStack.item as ItemArmor).armorType
}