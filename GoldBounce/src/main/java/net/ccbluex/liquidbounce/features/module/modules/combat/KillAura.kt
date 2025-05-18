val autoBlock by choices(
    "AutoBlock",
    arrayOf("Off", "Packet", "Fake", "QuickMarco", "BlocksMC", "BlocksMC_A", "BlocksMC_B", "HypixelFull", "NCP"),
    "Packet"
)

private var blocksmcAState = false
private var blocksmcAClickCounter = 0
private var blocksmcBState = false
private var blocksmcBTick = 0
private var ncpBlocking = false


private fun attackEntity(entity: EntityLivingBase, isLastClick: Boolean) {

    if (autoBlock == "BlocksMC_A" && (!blinked || !BlinkUtils.isBlinking)) {
        if (blockStatus) {
            sendPacket(C07PacketPlayerDigging(RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
            blockStatus = false
            blocksmcAClickCounter++
            if (blocksmcAClickCounter >= 7) {
                blocksmcAClickCounter = 0
            }
        } else {
            if (blocksmcAClickCounter < 7) {
                sendPacket(C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK))
            }
            sendPacket(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
            blockStatus = true
            blocksmcAState = true
        }
    }

    if (autoBlock == "BlocksMC_B" && (!blinked || !BlinkUtils.isBlinking)) {
        when (blocksmcBTick) {
            0 -> {
                sendPacket(C07PacketPlayerDigging(RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
                blocksmcBTick++
                blockStatus = false
            }
            1 -> {
                if (blocksmcAClickCounter < 7) {
                    sendPacket(C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK))
                }
                blocksmcBTick++
            }
            2 -> {
                blocksmcBTick++
            }
            3 -> {
                if (blocksmcAClickCounter < 7) {
                    sendPacket(C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK))
                }
                sendPacket(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
                blockStatus = true
                blocksmcBState = false
                blocksmcBTick = 0
                blocksmcAClickCounter = 0
            }
        }
    }


    if (autoBlock == "NCP" && (!blinked || !BlinkUtils.isBlinking)) {
        sendPacket(C07PacketPlayerDigging(RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
        blockStatus = false
        ncpBlocking = true
    }
}


@EventTarget
fun onPostMotion(event: MotionEvent) {
    if (target != null && autoBlock == "NCP") {
        sendPacket(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
        blockStatus = true
    }
}


private fun stopBlocking(forceStop: Boolean = false) {
    // ... existing code...

    if (autoBlock == "NCP" && ncpBlocking) {
        sendPacket(C07PacketPlayerDigging(RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
        ncpBlocking = false
    }

    // ... existing code...
}