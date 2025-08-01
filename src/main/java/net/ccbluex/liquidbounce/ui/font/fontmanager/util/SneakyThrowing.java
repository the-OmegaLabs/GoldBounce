/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.ui.font.fontmanager.util;

public final class SneakyThrowing {

	public static RuntimeException sneakyThrow(Throwable throwable) {
		return sneakyThrow0(throwable);
	}

	@SuppressWarnings("unchecked")
	private static <T extends Throwable> T sneakyThrow0(Throwable throwable) throws T {
		throw (T) throwable;
	}

	private SneakyThrowing() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}
}
