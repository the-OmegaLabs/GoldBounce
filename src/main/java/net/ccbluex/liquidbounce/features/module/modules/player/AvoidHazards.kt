package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value._boolean
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB

object AvoidHazards : Module("AvoidHazards", Category.WORLD) {
    private val fire by _boolean("Fire", true)
    private val cobweb by _boolean("Cobweb", true)
    private val cactus by _boolean("Cactus", true)
    private val lava by _boolean("Lava", true)
    private val water by _boolean("Water", true)
    private val plate by _boolean("PressurePlate", true)
    private val snow by _boolean("Snow", true)

    @EventTarget
    fun onBlockBB(e: BlockBBEvent) {
        val thePlayer = mc.thePlayer ?: return

        when (e.block) {
            Blocks.fire -> if (!fire) return

            Blocks.web -> if (!cobweb) return

            Blocks.snow -> if (!snow) return

            Blocks.cactus -> if (!cactus) return

            Blocks.water, Blocks.flowing_water ->
                // Don't prevent water from cancelling fall damage.
                if (!water || thePlayer.fallDistance >= 3.34627 || thePlayer.isInWater) return

            Blocks.lava, Blocks.flowing_lava -> if (!lava) return

            Blocks.wooden_pressure_plate, Blocks.stone_pressure_plate, Blocks.light_weighted_pressure_plate, Blocks.heavy_weighted_pressure_plate -> {
                if (plate)
                    e.boundingBox =
                        AxisAlignedBB(e.x.toDouble(), e.y.toDouble(), e.z.toDouble(), e.x + 1.0, e.y + 0.25, e.z + 1.0)
                return
            }

            else -> return
        }

        e.boundingBox = AxisAlignedBB(e.x.toDouble(), e.y.toDouble(), e.z.toDouble(), e.x + 1.0, e.y + 1.0, e.z + 1.0)
    }
}