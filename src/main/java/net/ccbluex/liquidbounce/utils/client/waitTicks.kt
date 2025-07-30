package net.ccbluex.liquidbounce.utils.client

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.ccbluex.liquidbounce.event.EventManager
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.event.Listenable
import java.util.EventListener
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun waitTicks(ticks: Int) {
    if (ticks <= 0) return

    val deferred = CompletableDeferred<Unit>()
    var elapsed = 0

    val listener = object : Listenable{
        @EventTarget
        fun onTick(event: GameTickEvent) {
            if (++elapsed >= ticks) {
                EventManager.unregisterListener(this)
                deferred.complete(Unit)
            }
        }
    }

    EventManager.registerListener(listener)
    deferred.await()
}
